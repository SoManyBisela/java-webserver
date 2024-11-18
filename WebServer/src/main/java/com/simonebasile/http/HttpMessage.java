package com.simonebasile.http;

public class HttpMessage<T> {
    protected final HttpHeaders headers;
    protected final T body;

    public HttpMessage(HttpHeaders headers, T body) {
        this.headers = headers;
        this.body = body;
    }

    public HttpMessage(HttpMessage<?> source,  T body) {
        this.headers = source.headers;
        this.body = body;
    }


    public HttpHeaders getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }
}
