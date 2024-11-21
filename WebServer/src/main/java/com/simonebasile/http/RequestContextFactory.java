package com.simonebasile.http;

/**
 * This interface is used to create request contexts.
 * It can be overridden by application developers to configure how request contexts are initialized by the web server.
 *
 * @param <C> the type of the request context
 */
public interface RequestContextFactory<C extends RequestContext> {
    C createContext();
}
