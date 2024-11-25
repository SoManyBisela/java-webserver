package com.simonebasile.http.handlers;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;

/**
 * A handler that dispatches the request to the appropriate method handler based on the HTTP method.
 * The default implementation returns a 405 Method Not Allowed response.
 * @param <Body> the type of the request body
 * @param <Context> the type of the context
 */
public class MethodHandler<Body, Context> implements HttpRequestHandler<Body, Context> {

    /**
     * Dispatches the request to the appropriate method handler based on the HTTP method.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    public HttpResponse<? extends HttpResponseBody> handle(HttpRequest<? extends Body> r, Context context) {
        return switch (r.getMethod()) {
            case "GET" -> handleGet(r, context);
            case "POST" -> handlePost(r, context);
            case "PUT" -> handlePut(r, context);
            case "DELETE" -> handleDelete(r, context);
            default -> methodNotAllowed();
        };
    }

    /**
     * Returns a 405 Method Not Allowed response.
     * @return the response
     */
    private HttpResponse<? extends HttpResponseBody> methodNotAllowed() {
        return new HttpResponse<>(405, new HttpHeaders(), new ByteResponseBody("method not allowed"));
    }

    /**
     * Handles a GET request.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed();
    }

    /**
     * Handles a POST request.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed();
    }

    /**
     * Handles a PUT request.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handlePut(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed();
    }

    /**
     * Handles a DELETE request.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handleDelete(HttpRequest<? extends Body> r, Context context) {
        return methodNotAllowed();
    }
}
