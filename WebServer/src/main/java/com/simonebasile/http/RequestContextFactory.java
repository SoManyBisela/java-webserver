package com.simonebasile.http;

public interface RequestContextFactory<C extends RequestContext> {
    C createContext();
}
