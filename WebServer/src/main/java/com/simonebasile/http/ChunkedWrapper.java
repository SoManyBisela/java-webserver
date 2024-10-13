package com.simonebasile.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ChunkedWrapper extends OutputStream {
    private final OutputStream target;
    public ChunkedWrapper(HttpOutputStream outputStream) {
        target = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        //TODO Should buffer this, to avoid sending a 5 bytes of length for a byte of data
        target.write("1\r\n".getBytes(StandardCharsets.UTF_8));
        target.write(b);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(String.valueOf(len).getBytes(StandardCharsets.UTF_8));
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
        super.write(b, off, len);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }
}
