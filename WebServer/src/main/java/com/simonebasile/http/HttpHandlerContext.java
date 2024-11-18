package com.simonebasile.http;

public interface HttpHandlerContext<Body, Context> {
    void registerHttpContext(String path, HttpRequestHandler<Body, ? super Context> handler);
    void registerHttpHandler(String path, HttpRequestHandler<Body, ? super Context> handler);
    void registerInterceptor(HttpInterceptor<Body, Context> preprocessor);
}
