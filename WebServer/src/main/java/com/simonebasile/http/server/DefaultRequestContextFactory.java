package com.simonebasile.http.server;

import com.simonebasile.http.handlers.RequestContext;

public class DefaultRequestContextFactory implements RequestContextFactory<RequestContext>{
    @Override
    public RequestContext createContext() {
        return new RequestContext();
    }
}
