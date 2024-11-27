package com.simonebasile.http.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that represents a WebSocket connection.
 * It provides methods to send and receive dataframes.
 *
 */
public class WebSocket implements Closeable{
    private final static Logger log = LoggerFactory.getLogger(WebSocket.class);
    private final ReentrantLock getDataframeLock;
    private boolean canGetDataframe;
    private final ReentrantLock sendDataframeLock;

    private final Socket connection;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;

    private boolean closeSent;

    private final static Random RNG = new Random();

    /**
     * Creates a new WebSocket.
     * @param connection the socket connection
     * @param inputStream the input stream of the connection
     * @param outputStream the output stream of the connection
     */
    public WebSocket(Socket connection, BufferedInputStream inputStream, BufferedOutputStream outputStream) {
        this.getDataframeLock = new ReentrantLock();
        this.canGetDataframe = true;
        this.sendDataframeLock = new ReentrantLock();

        this.connection = connection;
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        this.closeSent = false;
    }

    /**
     * A class that represents a WebSocket dataframe.
     * It holds a lock to prevent multiple threads from reading from the websocket while a dataframe is in use.
     */
    public class WSDataFrame implements Closeable{
        public final static int FIN = 0b1000;
        public final static int OP_TEXT = 1;
        public final static int OP_BIN = 2;
        public final static int OP_CLOSE = 8;
        public final static int OP_PING = 9;
        public final static int OP_PONG = 10;

        public final int flags;
        public final int opcode;
        public final boolean masked;
        public final long length;
        public final InputStream body;
        private final AtomicBoolean closed;

        private WSDataFrame(int flags, int opcode, boolean masked, long length, InputStream body ) {
            this.closed = new AtomicBoolean(false);
            this.flags = flags;
            this.opcode = opcode;
            this.masked = masked;
            this.length = length;
            this.body = body;
        }

        /**
         * Closes the dataframe and releases the lock.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public void close() throws IOException {
            if(closed.compareAndSet(false, true)) {
                canGetDataframe = true;
                body.close();
                getDataframeLock.unlock();
            }
        }
    }


    /**
     * Reads a dataframe from the input stream.
     * The returned dataframe holds a lock that must be closed before another dataframe can be read.
     *
     * @return the dataframe read
     * @throws IOException if an I/O error occurs.
     */
    public WSDataFrame getDataFrame() throws IOException {
        getDataframeLock.lock();
        if(!canGetDataframe) {
            getDataframeLock.unlock();
            throw new CustomException("A dataframe is already in use by this thread");
        }

        canGetDataframe = false;

        int flags, opcode;
        boolean masked;
        long length;
        InputStream body;

        try {
            // https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers#decoding_payload_length
            int unsignedByte = inputStream.read();
            if(unsignedByte == -1) {
                throw new EOFException();
            }
            flags = (0b11110000 & unsignedByte) >> 4;
            opcode = 0b00001111 & unsignedByte;
            unsignedByte = inputStream.read();
            if(unsignedByte == -1) {
                throw new EOFException();
            }
            masked = (0b10000000 & unsignedByte) == 0b10000000;

            //length
            int lengthByte = unsignedByte & 0b01111111;
            if(lengthByte == 126 || lengthByte == 127) {
                byte[] bytes = new byte[8];
                if(lengthByte == 126) {
                    //read next 2 bytes as length
                    //Offset is 6 to read into least significant bytes in big endian
                    inputStream.readNBytes(bytes, 6, 2);
                } else {
                    //read next 8 bytes as length
                    inputStream.readNBytes(bytes, 0, 8);
                    if(!((bytes[0] & 0b10000000) == 0)) {
                        throw new CustomException("Invalid Websocket frame length");
                    }
                }
                length = ByteBuffer.wrap(bytes).getLong();
            } else {
                length = lengthByte;
            }
            body = new FixedLengthInputStream(inputStream, length);
            //mask
            if(masked) {
                byte[] mask = new byte[4];
                inputStream.readNBytes(mask, 0, 4);
                body = new UnmaskingInputStream(body, mask);
            }
        } catch (Exception e) {
            getDataframeLock.unlock();
            throw e;
        }

        return new WSDataFrame(
                flags,
                opcode,
                masked,
                length,
                body
        );
    }

    /**
     * Sends a dataframe with the given flags, opcode and body.
     * The body is sent not masked.
     *
     * @param flags the flags of the dataframe
     * @param opcode the opcode of the dataframe
     * @param body the body of the dataframe
     * @throws IOException if an I/O error occurs.
     */
    public void sendUnmaskedDataframe(int flags, int opcode, byte[] body) throws IOException {
        sendDataFrameRaw(flags, opcode, body, null);
    }

    /**
     * Sends a dataframe with the given flags, opcode and body.
     * The body masked before sending it.
     * The mask is generated randomly.
     * The body array is copied to prevent modification of the original array.
     *
     * @param flags the flags of the dataframe
     * @param opcode the opcode of the dataframe
     * @param body the body of the dataframe
     * @throws IOException if an I/O error occurs.
     */
    public void maskAndSendDataframe(int flags, int opcode, byte[] body) throws IOException {
        byte[] mask = new byte[4];
        body = Arrays.copyOf(body, body.length);
        RNG.nextBytes(mask);
        for(int i = 0; i < body.length; i++) {
            body[i] ^= mask[i & 3];
        }
        sendDataFrameRaw(flags, opcode, body, mask);
    }

    /**
     * Sends a dataframe with the given flags, opcode, body and optionally a mask.
     * @param flags the flags of the dataframe
     * @param opcode the opcode of the dataframe
     * @param body the body of the dataframe
     * @param mask the mask used to mask the body if the dataframe is masked
     * @throws IOException
     */
    private void sendDataFrameRaw(int flags, int opcode, byte[] body, byte[] mask) throws IOException {
        boolean masked = mask != null;
        if(masked && mask.length != 4) {
            throw new CustomException("Invalid Websocket frame mask");
        }
        sendDataframeLock.lock();
        try {
            outputStream.write( (flags  << 4) | (opcode & 0b00001111) );
            if(body.length <= 125) {
                outputStream.write((body.length & 0b01111111) | (masked ? 0b10000000 : 0));
            } else if(body.length >= (1 << 16)) {
                outputStream.write(127 | (masked ? 0b10000000 : 0));
                byte[] len = new byte[8];
                ByteBuffer.wrap(len).putLong(body.length);
                outputStream.write(len);
            } else {
                outputStream.write(126 | (masked ? 0b10000000 : 0));
                byte[] len = new byte[2];
                ByteBuffer.wrap(len).putShort((short)body.length);
                outputStream.write(len);
            }
            if(masked) {
                outputStream.write(mask);
            }
            outputStream.write(body);
            outputStream.flush();
            if(opcode == WSDataFrame.OP_CLOSE) {
                closeSent = true;
            }
        } finally {
            sendDataframeLock.unlock();
        }
    }

    /**
     * Closes the underlying socket connection.
     * Sends a close dataframe if it has not been sent yet.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        try {
            sendDataframeLock.lock();
            try {
                getDataframeLock.lock();
                if(!canGetDataframe) {
                    log.error("Closing a socket with open dataframes. Any further attempts at using the dataframe will fail");
                }
                if(!closeSent) {
                    sendUnmaskedDataframe(WSDataFrame.FIN, WSDataFrame.OP_CLOSE, new byte[0]);
                }
                connection.close();
            } finally {
                getDataframeLock.unlock();
            }
        } finally {
            sendDataframeLock.unlock();
        }
    }

    public boolean isCloseSent() {
        return closeSent;
    }

}
