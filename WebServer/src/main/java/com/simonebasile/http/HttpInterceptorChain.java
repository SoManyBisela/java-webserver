package com.simonebasile.http;

public interface HttpInterceptorChain<T> {

    HttpResponse<? extends HttpResponse.ResponseBody> next(HttpRequest<T> req);

}
