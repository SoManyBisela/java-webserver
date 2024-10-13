package com.simonebasile.http;

public class HttpMessage<T> {
    protected final HttpVersion version;
    protected final HttpHeaders headers;
    protected final T body;

    public HttpMessage(HttpVersion version, HttpHeaders headers, T body) {
        this.version = version;
        this.headers = headers;
        this.body = body;
    }
}
