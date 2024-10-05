package com.simonebasile.http;

public interface HttpRequestHandler<T> {
    void handle(HttpRequest<T> r, HttpOutputStream out);
}
