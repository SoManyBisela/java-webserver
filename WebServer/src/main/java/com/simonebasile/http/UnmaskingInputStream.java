package com.simonebasile.http;

import java.io.IOException;
import java.io.InputStream;

public class UnmaskingInputStream extends InputStream {
    private final InputStream source;
    private final byte[] mask;
    private int index;

    public UnmaskingInputStream(InputStream source, byte[] mask) {
        this.source = source;
        this.mask = mask;
        this.index = 0;
    }

    @Override
    public int read() throws IOException {
        int r = source.read();
        if( r == -1) {
            return -1;
        }
        return mask[getAndIncrement()] ^ r;
    }

    private int getAndIncrement() {
        int r = index;
        index = (index + 1) % mask.length;
        return r;
    }

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

    @Override
    public void close() throws IOException {
        source.close();
    }

}
