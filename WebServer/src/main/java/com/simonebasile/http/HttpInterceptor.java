package com.simonebasile.http;

public interface HttpInterceptor<T> {
    HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<? extends T> request, HttpRequestHandler<T> next);
}
