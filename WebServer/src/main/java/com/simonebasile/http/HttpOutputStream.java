package com.simonebasile.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpOutputStream extends BufferedOutputStream {
    private final byte[] SPACE = " ".getBytes(StandardCharsets.UTF_8);
    private final byte[] NL = "\r\n".getBytes(StandardCharsets.UTF_8);
    private final byte[] COLON = ": ".getBytes(StandardCharsets.UTF_8);

    public HttpOutputStream(OutputStream out) {
        super(out);
    }

    public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }

    public void writeStatus(HttpVersion version, int status, String statusString) throws IOException {
        write(version.value);
        space();
        write(Integer.toString(status));
        space();
        write(statusString);
        nl();
    }

    public void writeHeader(String key, String value) throws IOException {
        write(key);
        colon();
        write(value);
        nl();
    }


    public void writeBody(byte[] body) throws IOException {
        writeHeader("Content-Length", Integer.toString(body.length));
        nl();
        write(body);
        flush();
    }

    public void endHeaders() throws IOException {
        nl();
    }
    public void end() throws IOException {
        nl();
        flush();
    }

    private void space() throws IOException {
        write(SPACE);
    }

    private void nl() throws IOException {
        write(NL);
    }

    private void colon() throws IOException {
        write(COLON);
    }
}
