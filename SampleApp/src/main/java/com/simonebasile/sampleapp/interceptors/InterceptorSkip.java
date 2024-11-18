package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;

import java.util.function.Predicate;

public abstract class InterceptorSkip<T, C> implements HttpInterceptor<T, C> {
    protected final HttpInterceptor<T, C> target;

    public InterceptorSkip(HttpInterceptor<T, C> target) {
        this.target = target;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<? extends T> request, C ctx, HttpRequestHandler<T, C> next) {
        if(shouldIntercept(request)) {
            return target.preprocess(request, ctx, next);
        } else {
            return next.handle(request, ctx);
        }
    }

    protected abstract boolean shouldIntercept(HttpRequest<? extends T> request);

    public static <T, C> InterceptorSkip<T, C> fromPredicate(HttpInterceptor<T, C> target, Predicate<HttpRequest<? extends T>> test) {
        return new InterceptorSkip<>(target) {
            @Override
            protected boolean shouldIntercept(HttpRequest<? extends T> request) {
                return test.test(request);
            }
        };
    }

}
