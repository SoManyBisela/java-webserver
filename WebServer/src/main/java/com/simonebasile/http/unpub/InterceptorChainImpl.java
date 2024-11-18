package com.simonebasile.http.unpub;

import com.simonebasile.http.*;

import java.util.List;

public class InterceptorChainImpl<Body, Context> implements HttpRequestHandler<Body, Context> {
    private final List<HttpInterceptor<Body, Context>> interceptors;
    private final HttpRequestHandler<Body, ? super Context> handler;
    private int toProcess;

    public InterceptorChainImpl(List<HttpInterceptor<Body, Context>> interceptors, HttpRequestHandler<Body, ? super Context> handler) {
        this.interceptors = interceptors;
        this.handler = handler;
        toProcess = 0;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends Body> r, Context context) {
        if(toProcess < interceptors.size()) {
            return interceptors.get(toProcess++).preprocess(r, context, this);
        } else {
            return handler.handle(r, context);
        }
    }
}
