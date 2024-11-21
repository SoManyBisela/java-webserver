package com.simonebasile.http.response;

import com.simonebasile.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A response body that writes a byte array to the output stream.
 */
public class ByteResponseBody implements HttpResponse.ResponseBody {
    private final byte[] content;
    private final String contentType;

    /**
     * Creates a new byte response body.
     * The content type is set to "application/octet-stream".
     * @param content the content
     */
    public ByteResponseBody(byte[] content) {
        this.content = content;
        this.contentType = "application/octet-stream";
    }

    /**
     * Creates a new byte response body.
     * The content type is set to "text/plain; charset=utf-8".
     * @param content the content
     */
    public ByteResponseBody(String content) {

        this(content, StandardCharsets.UTF_8);

    }

    /**
     * Creates a new byte response body.
     * @param content the content
     * @param contentType the content type
     */
    public ByteResponseBody(String content, String contentType) {
        this(content, StandardCharsets.UTF_8, contentType);
    }

    /**
     * Creates a new byte response body.
     * @param content the content
     * @param charset the charset
     */
    public ByteResponseBody(String content, Charset charset) {
        this(content, charset, "text/plain; charset=" + charset);
    }

    /**
     * Creates a new byte response body.
     * @param content the content
     * @param charset the charset
     * @param contentType the content type
     */
    public ByteResponseBody(String content, Charset charset, String contentType) {
        this.content = content.getBytes(charset);
        this.contentType = contentType;
    }

    /**
     * Creates a new byte response body.
     * @param content the content
     * @param contentType the content type
     */
    public ByteResponseBody(byte[] content, String contentType) {
        this.content = content;
        this.contentType = Objects.requireNonNull(contentType, "contentType cannot be null");
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(content);
    }

    @Override
    public Long contentLength() {
        return (long)content.length;
    }

    @Override
    public String contentType() {
        return contentType;
    }
}
