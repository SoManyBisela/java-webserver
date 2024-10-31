package com.simonebasile.http.unpub;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpInputStream extends BufferedInputStream {

    private final static int MAX_HEADER_SIZE = 4096;

    public HttpInputStream(InputStream in) {
        super(in);
    }

    public String readLine() throws IOException {
        //TODO consider using parent buffer
        byte[] buf = new byte[MAX_HEADER_SIZE];
        int bind = 0;
        int read;
        boolean expend = false;
        while ((read = read()) != '\n') {
            if (expend) {
                throw new CustomException("Expected exception");
            }
            if (read == -1) {
                throw new EOFException();
            }
            if (read == '\r') {
                expend = true;
            } else {
                buf[bind++] = (byte) read;
            }
        }
        return new String(buf, 0, bind, StandardCharsets.UTF_8);
    }
}
