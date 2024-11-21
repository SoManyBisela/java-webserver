package com.simonebasile.http.unpub;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to unmask the bytes of a stream using a mask.
 * The mask is applied cyclically to the bytes of the stream.
 */
public class UnmaskingInputStream extends InputStream {
    private final InputStream source;
    private final byte[] mask;
    private int index;

    public UnmaskingInputStream(InputStream source, byte[] mask) {
        this.source = source;
        this.mask = mask;
        this.index = 0;
    }

    /**
     * Reads a byte from the input stream.
     * @return the byte read xor-ed with the mask
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        int r = source.read();
        if( r == -1) {
            return -1;
        }
        return mask[getAndIncrement()] ^ r;
    }

    /**
     * Gets the next index of the mask and increments the index.
     * @return the next index of the mask
     */
    private int getAndIncrement() {
        int r = index;
        index = (index + 1) % mask.length;
        return r;
    }

    /**
     * Reads up to {@code len} bytes of data from the source stream into an array of bytes after unmasking it.
     * @param b     the buffer into which the data is read.
     * @param off   the start offset in array {@code b}
     *                   at which the data is written.
     * @param len   the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int rn = source.read(b, off, len);
        for(int i = 0; i < rn; i++) {
            b[off + i] ^= mask[getAndIncrement()];
        }
        return rn;
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    /**
     * Closes the source stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        source.close();
    }

}
