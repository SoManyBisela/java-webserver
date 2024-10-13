package com.simonebasile.http;

public interface HttpRequestHandler<T> {
    HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<T> r);
}
