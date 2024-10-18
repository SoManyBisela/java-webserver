package com.simonebasile.http;

public interface HttpInterceptor<T> {
    HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<T> request, HttpRequestHandler<T> next);
}
