package com.simonebasile.http;

public interface HttpRequestHandlerNC<T> {
    HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends T> r);
}
