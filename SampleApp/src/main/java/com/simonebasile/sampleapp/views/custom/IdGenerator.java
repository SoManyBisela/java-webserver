package com.simonebasile.sampleapp.views.custom;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to generate unique IDs for HTML elements.
 */
public class IdGenerator {
    private final static AtomicInteger id =  new AtomicInteger();

    public static String get() {
        return "auto" + id.incrementAndGet();
    }
}
