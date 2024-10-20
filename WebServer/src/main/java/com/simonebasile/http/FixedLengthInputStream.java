package com.simonebasile.http;

import java.io.IOException;
import java.io.InputStream;

public class FixedLengthInputStream extends InputStream {
    private final InputStream source;
    private long length;

    public FixedLengthInputStream(InputStream source, long length) {
        this.source = source;
        this.length = length;
    }

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

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
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

    @Override
    public int available() throws IOException {
        return (int) Math.min(length, this.source.available());
    }

    @Override
    public void close() throws IOException {
        skipNBytes(length);
    }
}
