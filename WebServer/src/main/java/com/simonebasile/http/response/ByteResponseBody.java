package com.simonebasile.http.response;

import com.simonebasile.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ByteResponseBody implements HttpResponse.ResponseBody {
    private final byte[] content;
    private final String contentType;

    public ByteResponseBody(byte[] content) {
        this.content = content;
        this.contentType = "application/octet-stream";
    }

    public ByteResponseBody(String content) {

        this(content, StandardCharsets.UTF_8);

    }
    public ByteResponseBody(String content, String contentType) {
        this(content, StandardCharsets.UTF_8, contentType);
    }

    public ByteResponseBody(String content, Charset charset) {
        this(content, charset, "text/plain; charset=" + charset);
    }

    public ByteResponseBody(String content, Charset charset, String contentType) {
        this.content = content.getBytes(charset);
        this.contentType = contentType;
    }

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
