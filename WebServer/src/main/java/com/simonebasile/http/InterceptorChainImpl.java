package com.simonebasile.http;

import java.io.InputStream;
import java.util.List;

public class InterceptorChainImpl<T> implements HttpRequestHandler<T>{
    private final List<HttpInterceptor<T>> interceptors;
    private final HttpRequestHandler<T> handler;
    private int toProcess;

    public InterceptorChainImpl(List<HttpInterceptor<T>> interceptors, HttpRequestHandler<T> handler) {
        this.interceptors = interceptors;
        this.handler = handler;
        toProcess = 0;
    }


    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<T> r) {
        if(toProcess < interceptors.size()) {
            return interceptors.get(toProcess++).preprocess(r, this);
        } else {
            return handler.handle(r);
        }
    }
}
