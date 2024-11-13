package com.simonebasile.http;

import com.simonebasile.http.unpub.InterceptorChainImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestInterceptorChain {

    @Test
    public void testInterceptorChain() {

        boolean[] called = new boolean[1];

        List<HttpInterceptor<Integer>> httpInterceptorList = new ArrayList<>();
        httpInterceptorList.add((r, next) -> {
            HttpRequest<Integer> oneReq = new HttpRequest<>("", "", HttpVersion.V1_1, new HttpHeaders(), 1);
            called[0] = true;
            return next.handle(oneReq);
        });
        HttpRequestHandler<Integer> handler = (r) -> {
            Assertions.assertEquals(1, r.getBody());
            return null;
        };
        InterceptorChainImpl<Integer> objectInterceptorChain = new InterceptorChainImpl<>(httpInterceptorList, handler);
        HttpRequest<Integer> zeroReq = new HttpRequest<>("", "", HttpVersion.V1_1, new HttpHeaders(), 1);
        objectInterceptorChain.handle(zeroReq);

        Assertions.assertTrue(called[0]);
    }

}
