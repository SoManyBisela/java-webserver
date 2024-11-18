package com.simonebasile.http;

public interface HttpInterceptor<Body, Context> {
    HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<? extends Body> request, Context requestContext, HttpRequestHandler<Body, Context> next);
}
