package com.simonebasile.http.unpub;

import com.simonebasile.http.HttpVersion;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An output stream that add some utility methods to write HTTP messages.
 */
public class HttpOutputStream extends BufferedOutputStream {
    private final byte[] SPACE = " ".getBytes(StandardCharsets.UTF_8);
    private final byte[] NL = "\r\n".getBytes(StandardCharsets.UTF_8);
    private final byte[] COLON = ": ".getBytes(StandardCharsets.UTF_8);

    public HttpOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes a string to the output stream.
     *
     * @param s the string to write.
     * @throws IOException if an I/O error occurs.
     */
    public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * writes the status line to the output stream.
     *
     * @param version      the HTTP version.
     * @param status       the status code.
     * @param statusString the status string.
     * @throws IOException if an I/O error occurs.
     */
    public void writeStatus(HttpVersion version, int status, String statusString) throws IOException {
        write(version.value);
        space();
        write(Integer.toString(status));
        space();
        write(statusString);
        nl();
    }

    /**
     * Writes a header to the output stream.
     *
     * @param key   the header name.
     * @param value the header value.
     * @throws IOException if an I/O error occurs.
     */
    public void writeHeader(String key, String value) throws IOException {
        write(key);
        colon();
        write(value);
        nl();
    }


    /**
     * Writes the content length and body to the output stream.
     *
     * @param body the body to write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeBody(byte[] body) throws IOException {
        writeHeader("Content-Length", Integer.toString(body.length));
        nl();
        write(body);
        flush();
    }

    public void endHeaders() throws IOException {
        nl();
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
