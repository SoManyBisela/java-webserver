package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;

public class MethodHandler<T> implements HttpRequestHandler<T> {

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends T> r) {
        return switch (r.getMethod()) {
            case "GET" -> handleGet(r);
            case "POST" -> handlePost(r);
            case "PUT" -> handlePut(r);
            case "DELETE" -> handleDelete(r);
            default -> methodNotAllowed(r);
        };
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> methodNotAllowed(HttpRequest<? extends T> r) {
        return new HttpResponse<>(r.getVersion(), 405, new HttpHeaders(), null);
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<? extends T> r) {
        return methodNotAllowed(r);
    }
}
