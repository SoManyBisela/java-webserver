package com.simonebasile.http.unpub;

import java.io.IOException;
import java.io.InputStream;


/**
 * An input stream that reads a fixed number of bytes from the source stream.
 * Calling close() will consume the remaining bytes.
 */
public class FixedLengthInputStream extends InputStream {
    private final InputStream source;
    private long length;

    /**
     * Creates a new FixedLengthInputStream.
     * @param source the source stream
     * @param length the number of bytes to read
     */
    public FixedLengthInputStream(InputStream source, long length) {
        this.source = source;
        this.length = length;
    }

    /**
     * reads a byte from the source stream.
     * if all bytes have been read, returns -1.
     * @return the byte read, or -1 if the end of the stream has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        if(length > 0) {
            final int read = source.read();
            if (read != -1) {
                length--;
            }
            return read;
        }
        return -1;
    }

    /**
     * reads up to {@code len} bytes of data from the source stream into an array of bytes.
     * if length is less than {@code len}, reads all remaining bytes.
     * @param b     the buffer into which the data is read.
     * @param off   the start offset in array {@code b}
     *                   at which the data is written.
     * @param len   the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(len == 0) return 0;
        len = (int)Math.min(len, this.length);
        if(len == 0) {
            return -1;
        }
        int read = source.read(b, off, len);
        if(read > 0) {
            length -= read;
            return read;
        } else {
            return  -1;
        }
    }


    /**
     * @return the number of bytes that can be read from the source stream without blocking.
     * @throws IOException
     */
    @Override
    public int available() throws IOException {
        return (int) Math.min(length, this.source.available());
    }

    /**
     * Consumes remaining bytes from the source stream.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        skipNBytes(length);
    }
}
