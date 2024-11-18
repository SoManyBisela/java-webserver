package com.simonebasile.http;

public interface HttpRequestHandler<Body, Context> {
    HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends Body> r, Context requestContext);
}
