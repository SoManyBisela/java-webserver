package com.simonebasile.http;

import com.simonebasile.http.handlers.HttpInterceptor;
import com.simonebasile.http.handlers.HttpRequestHandler;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.http.unexported.InterceptorChainImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestInterceptorChain {

    @Test
    public void testInterceptorChain() {

        boolean[] called = new boolean[1];

        List<HttpInterceptor<Integer, Void>> httpInterceptorList = new ArrayList<>();
        httpInterceptorList.add((r, c, next) -> {
            HttpRequest<Integer> oneReq = new HttpRequest<>("", "", HttpVersion.V1_1, new HttpHeaders(), 1);
            called[0] = true;
            return next.handle(oneReq, c);
        });
        HttpRequestHandler<Integer, Void> handler = (r, c) -> {
            Assertions.assertEquals(1, r.getBody());
            return null;
        };
        InterceptorChainImpl<Integer, Void> objectInterceptorChain = new InterceptorChainImpl<>(httpInterceptorList, handler);
        HttpRequest<Integer> zeroReq = new HttpRequest<>("", "", HttpVersion.V1_1, new HttpHeaders(), 1);
        objectInterceptorChain.handle(zeroReq, null);

        Assertions.assertTrue(called[0]);
    }

}
