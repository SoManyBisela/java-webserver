package com.simonebasile.http.handlers;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;

public class MethodHandler<Body, Context> implements HttpRequestHandler<Body, Context> {

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends Body> r, Context context) {
        return switch (r.getMethod()) {
            case "GET" -> handleGet(r, context);
            case "POST" -> handlePost(r, context);
            case "PUT" -> handlePut(r, context);
            case "DELETE" -> handleDelete(r, context);
            default -> methodNotAllowed(r);
        };
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> methodNotAllowed(HttpRequest<? extends Body> r) {
        return new HttpResponse<>(r.getVersion(), 405, new HttpHeaders(), new ByteResponseBody("method not allowed"));
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed(r);
    }
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed(r);
    }
}
