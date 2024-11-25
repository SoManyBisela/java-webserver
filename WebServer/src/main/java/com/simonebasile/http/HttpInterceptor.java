package com.simonebasile.http;

import com.simonebasile.http.response.HttpResponseBody;

/**
 * This interface is used to intercept HTTP requests before they are handled by the server.
 *
 * @param <Body> the type of the body of the HTTP request
 * @param <Context> the type of the context object that is passed to the handlers
 */
public interface HttpInterceptor<Body, Context> {

    /**
     * This method is called before the request is handled by the server.
     *
     * @param request the request to preprocess
     * @param requestContext the context of the request
     * @param next the next handler in the chain
     * @return the response to send to the client
     */
    HttpResponse<? extends HttpResponseBody> intercept(HttpRequest<? extends Body> request, Context requestContext, HttpRequestHandler<Body, Context> next);
}
