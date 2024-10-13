package com.simonebasile.http;

public interface HttpHandlerContext<T> {
    void registerHttpContext(String path, HttpRequestHandler<T> handler);
    void registerHttpHandler(String path, HttpRequestHandler<T> handler);
    void registerPreprocessor(HttpRequestPreprocessor<T> preprocessor);
}
