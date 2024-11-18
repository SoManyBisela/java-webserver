package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.*;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;

public class MethodHandler<T> implements HttpRequestHandler<T, ApplicationRequestContext> {

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends T> r, ApplicationRequestContext context) {
        return switch (r.getMethod()) {
            case "GET" -> handleGet(r, context);
            case "POST" -> handlePost(r, context);
            case "PUT" -> handlePut(r, context);
            case "DELETE" -> handleDelete(r, context);
            default -> methodNotAllowed(r);
        };
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> methodNotAllowed(HttpRequest<? extends T> r) {
        return new HttpResponse<>(r.getVersion(), 405, new HttpHeaders(), null);
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends T> r, ApplicationRequestContext context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends T> r, ApplicationRequestContext context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends T> r, ApplicationRequestContext context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<? extends T> r, ApplicationRequestContext context) {
        return methodNotAllowed(r);
    }
}
