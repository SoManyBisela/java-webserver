package com.simonebasile.http.handlers;

import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;

/**
 * This interface is used to handle HTTP requests.
 * It can be overridden by application developers to define custom request handlers.
 *
 * @param <Body> the type of the body of the HTTP request
 * @param <Context> the type of the context object that is passed to the handlers
 */
public interface HttpRequestHandler<Body, Context> {
    /**
     * This method is called to handle an HTTP request.
     *
     * @param r the request to handle
     * @param requestContext the context of the request
     * @return the response to send to the client
     */
    HttpResponse<? extends HttpResponseBody> handle(HttpRequest<? extends Body> r, Context requestContext);
}
