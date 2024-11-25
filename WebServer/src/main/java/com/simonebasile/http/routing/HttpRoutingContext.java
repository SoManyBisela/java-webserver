package com.simonebasile.http.routing;

import com.simonebasile.http.handlers.HttpInterceptor;
import com.simonebasile.http.handlers.HttpRequestHandler;

/**
 * This interface is used to register handlers and interceptors for HTTP requests.
 *
 * @param <Body> the type of the body of the HTTP request
 * @param <Context> the type of the context object that is passed to the handlers
 */
public interface HttpRoutingContext<Body, Context> {

    /**
     * Registers a context for a specific path.
     * Http contexts are handlers that are used for all subpaths of the path they are registered for unless a more specific handler is registered.
     *
     * @param path the path to register the handler for
     * @param handler the handler to register
     */
    void registerHttpContext(String path, HttpRequestHandler<Body, ? super Context> handler);

    /**
     * Registers a handler for a specific path.
     *
     * @param path the path to register the handler for
     * @param handler the handler to register
     */
    void registerHttpHandler(String path, HttpRequestHandler<Body, ? super Context> handler);

    /**
     * Registers an interceptor that is executed before the handler is executed.
     *
     * @param interceptor the interceptor to register
     */
    void registerInterceptor(HttpInterceptor<Body, Context> interceptor);
}
