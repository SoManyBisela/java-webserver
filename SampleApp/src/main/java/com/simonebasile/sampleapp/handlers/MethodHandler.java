package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;

public class MethodHandler<T> implements HttpRequestHandler<T> {

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<T> r) {
        return switch (r.getMethod()) {
            case "GET" -> handleGet(r);
            case "POST" -> handlePost(r);
            case "PUT" -> handlePut(r);
            case "DELETE" -> handleDelete(r);
            default -> methodNotAllowed(r);
        };
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> methodNotAllowed(HttpRequest<T> r) {
        return new HttpResponse<>(r.getVersion(), 405, new HttpHeaders(), null);
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<T> r) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<T> r) {
        return methodNotAllowed(r);
    }
}
