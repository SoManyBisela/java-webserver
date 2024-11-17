package com.simonebasile.sampleapp.views.html.custom;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private final static AtomicInteger id =  new AtomicInteger();

    public static String get() {
        return "auto" + id.incrementAndGet();
    }
}
