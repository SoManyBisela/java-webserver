package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.HttpInterceptor;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;

import java.util.function.Predicate;

public abstract class InterceptorSkip<T> implements HttpInterceptor<T> {
    protected final HttpInterceptor<T> target;

    public InterceptorSkip(HttpInterceptor<T> target) {
        this.target = target;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<? extends T> request, HttpRequestHandler<T> next) {
        if(shouldIntercept(request)) {
            return target.preprocess(request, next);
        } else {
            return next.handle(request);
        }
    }

    protected abstract boolean shouldIntercept(HttpRequest<? extends T> request);

    public static <T> InterceptorSkip<T> fromPredicate(HttpInterceptor<T> target, Predicate<HttpRequest<? extends T>> test) {
        return new InterceptorSkip<>(target) {
            @Override
            protected boolean shouldIntercept(HttpRequest<? extends T> request) {
                return test.test(request);
            }
        };
    }

}
