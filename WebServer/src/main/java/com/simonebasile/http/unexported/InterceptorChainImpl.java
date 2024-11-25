package com.simonebasile.http.unexported;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ResponseBody;

import java.util.List;

/**
 * An implementation of HttpRequestHandler that chains multiple HttpInterceptors.
 * The interceptors are executed in the order they are passed to the constructor.
 * The handler is executed after all the interceptors.
 */
public class InterceptorChainImpl<Body, Context> implements HttpRequestHandler<Body, Context> {
    private final List<HttpInterceptor<Body, Context>> interceptors;
    private final HttpRequestHandler<Body, ? super Context> handler;
    private int toProcess;

    /**
     * Creates a new InterceptorChainImpl.
     * @param interceptors the list of interceptors to execute
     * @param handler the handler to execute
     */
    public InterceptorChainImpl(List<HttpInterceptor<Body, Context>> interceptors, HttpRequestHandler<Body, ? super Context> handler) {
        this.interceptors = interceptors;
        this.handler = handler;
        toProcess = 0;
    }

    /**
     * Executes the next interceptor in the chain.
     * If all the interceptors have been executed, the handler is executed.
     * @param r the request to process
     * @param context the context of the request
     * @return the response of the interceptor or the handler
     */
    @Override
    public HttpResponse<? extends ResponseBody> handle(HttpRequest<? extends Body> r, Context context) {
        if(toProcess < interceptors.size()) {
            return interceptors.get(toProcess++).intercept(r, context, this);
        } else {
            return handler.handle(r, context);
        }
    }
}
