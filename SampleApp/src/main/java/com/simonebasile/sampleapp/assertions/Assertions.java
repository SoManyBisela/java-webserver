package com.simonebasile.sampleapp.assertions;

public class Assertions {

    public static void assertTrue(boolean value) {
        if(!value) {
            throw new ArithmeticException("assertTrue failed");
        }
    }

}
