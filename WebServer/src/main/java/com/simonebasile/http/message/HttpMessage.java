package com.simonebasile.http.message;

/**
 * This class represents an HTTP message, which contains all common elements of an HTTP request or response.
 *
 * @param <T> the type of the body of the HTTP message
 */
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
