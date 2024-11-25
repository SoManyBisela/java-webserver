package com.simonebasile.http.server;

public class DefaultRequestContextFactory implements RequestContextFactory<RequestContext>{
    @Override
    public RequestContext createContext() {
        return new RequestContext();
    }
}
