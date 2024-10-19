package com.simonebasile.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public class WebSocket implements Closeable{
    private final static Logger log = LoggerFactory.getLogger(WebSocket.class);
    private final ReentrantLock getDataframeLock;
    private boolean canGetDataframe;
    private final ReentrantLock sendDataframeLock;

    private final HttpRequest<Void> connectionRequest;
    private final Socket connection;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;


    public WebSocket(HttpRequest<Void> connectionRequest, Socket connection, BufferedInputStream inputStream, BufferedOutputStream outputStream) {
        this.getDataframeLock = new ReentrantLock();
        this.canGetDataframe = true;
        this.sendDataframeLock = new ReentrantLock();

        this.connectionRequest = connectionRequest;
        this.connection = connection;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public class WSDataFrame implements Closeable{
        private final int flags;
        private final int opcode;
        private final boolean masked;
        private final long length;
        private final InputStream body;
        private boolean closed;

        public WSDataFrame(int flags, int opcode, boolean masked, long length, InputStream body ) {
            this.flags = flags;
            this.opcode = opcode;
            this.masked = masked;
            this.length = length;
            this.body = body;
        }

        @Override
        public void close() throws IOException {
            if(!closed) {
                //TODO review
                closed = true;
                canGetDataframe = true;
                getDataframeLock.unlock();
                body.close();
            }
        }
    }

    /*
    public static void main(String[] args) {
        int source = 255;
        byte r = (byte)source;
        System.out.println(r);
        System.out.println((r & 0b10000000));
        System.out.println((byte)(r & 0b10000000));

        for(int i = 0; i < 255; i++) {
            byte t = (byte)i;
            boolean isMSB0 = (t & 0b10000000) == 0;
            System.out.println("Int [" + i + "] is Byte [" + t + "] with isMSB0 [" + isMSB0 + "]");
        }

    }
     */

    private boolean canRead() {
        try {
            return inputStream.available() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private WSDataFrame getDataFrame() throws IOException {
        //TODO handle exceptions
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
            flags = (0b11110000 & unsignedByte) >> 4;
            opcode = 0b00001111 & unsignedByte;
            unsignedByte = inputStream.read();
            masked = (0b10000000 & unsignedByte) == 0b10000000;

            //length
            int lengthByte = unsignedByte & 0b011111111;
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

    public void sendDataFrameRaw(int flags, int opcode, byte[] mask, byte[] body) throws IOException {
        boolean masked = mask != null;
        if(masked && mask.length != 4) {
            throw new CustomException("Invalid Websocket frame mask");
        }
        sendDataframeLock.lock();
        try {
            outputStream.write( (flags  << 4) | (opcode & 0b00001111) );
            if(body.length < 125) {
                outputStream.write((body.length & 0b01111111) | (masked ? 0b10000000 : 0));
            } else if(body.length >= (1 << 16)) {
                outputStream.write(127 | (masked ? 0b10000000 : 0));
                byte[] len = new byte[4];
                ByteBuffer.wrap(len).putInt(body.length);
                outputStream.write(len);
            } else {
                outputStream.write(126 | (masked ? 0b10000000 : 0));
                byte[] len = new byte[8];
                ByteBuffer.wrap(len).putLong(body.length);
                outputStream.write(len);
            }
            if(masked) {
                outputStream.write(mask);
            }
            outputStream.write(body);
            outputStream.flush();
        } finally {
            sendDataframeLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        if(!canGetDataframe) {
            log.error("Closing a socket with open dataframes. Any further attempts at using the dataframe will fail");
        }
        connection.close();
    }


}
