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
        target.write("1\r\n".getBytes(StandardCharsets.UTF_8));
        target.write(b);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        //A zero length chunk means the body is finished
        //So if the length is zero we return to avoid sending the chunk unintentionally
        if(len == 0) return;

        target.write(Integer.toString(len, 16).getBytes(StandardCharsets.UTF_8));
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
        target.write(b, off, len);
        target.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public void close() throws IOException {
        target.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
