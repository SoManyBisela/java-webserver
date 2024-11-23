package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;

import java.util.function.Predicate;

/**
 * An interceptor that conditionally delegates to another interceptor.
 * @param <T> the request body type
 * @param <C> the context type
 */
public abstract class ConditionalInterceptor<T, C> implements HttpInterceptor<T, C> {
    protected final HttpInterceptor<T, C> target;

    public ConditionalInterceptor(HttpInterceptor<T, C> target) {
        this.target = target;
    }

    /**
     * Intercepts the request and delegates to the target interceptor if the {@link #shouldIntercept(HttpRequest)} method returns true.
     * @param request the request
     * @param ctx the context
     * @param next the next handler
     * @return the response
     */
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> intercept(HttpRequest<? extends T> request, C ctx, HttpRequestHandler<T, C> next) {
        if(shouldIntercept(request)) {
            return target.intercept(request, ctx, next);
        } else {
            return next.handle(request, ctx);
        }
    }

    /**
     * Decides whether to intercept the request.
     * @param request the request
     * @return true if the request should be intercepted
     */
    protected abstract boolean shouldIntercept(HttpRequest<? extends T> request);

    /**
     * Creates a conditional interceptor from a predicate.
     * @param target the target interceptor
     * @param test the predicate
     * @param <T> the request body type
     * @param <C> the context type
     * @return the conditional interceptor
     */
    public static <T, C> ConditionalInterceptor<T, C> fromPredicate(HttpInterceptor<T, C> target, Predicate<HttpRequest<? extends T>> test) {
        return new ConditionalInterceptor<>(target) {
            @Override
            protected boolean shouldIntercept(HttpRequest<? extends T> request) {
                return test.test(request);
            }
        };
    }

}
