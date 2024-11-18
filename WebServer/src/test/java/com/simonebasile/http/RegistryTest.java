package com.simonebasile.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryTest {

    private <T> T getHandler(HandlerRegistry<T> reg, String path) {
        var m = reg.getHandler(path);
        if(m == null) return null;
        return m.handler();
    }

    @Test
    void testContext() {
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();
        Object o = new Object();
        handlerRegistry.insertCtx("/lama", o);
        Assertions.assertEquals(o, getHandler(handlerRegistry, "/lama"));
        Assertions.assertEquals(o, getHandler(handlerRegistry, "/lama/cane"));
        Assertions.assertEquals(o, getHandler(handlerRegistry, "/lama/costruzione"));
        Assertions.assertEquals(o, getHandler(handlerRegistry, "/lama/altroPathLungo"));
        Assertions.assertNull(getHandler(handlerRegistry, "/alpaca"));
    }

    @Test
    void testExact() {
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();
        Object o = new Object();
        handlerRegistry.insertExact("/lama/pinguino", o);
        Assertions.assertEquals(o, getHandler(handlerRegistry, "/lama/pinguino"));
        Assertions.assertNull(getHandler(handlerRegistry, "/lama"));
        Assertions.assertNull(getHandler(handlerRegistry, "/lama/cane"));
        Assertions.assertNull(getHandler(handlerRegistry, "/lama/costruzione"));
        Assertions.assertNull(getHandler(handlerRegistry, "/lama/altroPathLungo"));
        Assertions.assertNull(getHandler(handlerRegistry, "/alpaca"));
    }

    @Test
    void testMoreFitting() {
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();
        Object lamaCtx = new Object();
        Object lamaPinguinoCtx = new Object();
        Object lamaPinguinoEx = new Object();
        Object lamaPinguinoArmadilloCtx = new Object();
        Object gattoCanguroEx = new Object();

        handlerRegistry.insertCtx("/lama", lamaCtx);
        handlerRegistry.insertCtx("/lama/pinguino", lamaPinguinoCtx);
        handlerRegistry.insertExact("/lama/pinguino", lamaPinguinoEx);
        handlerRegistry.insertCtx("/lama/pinguino/armadillo", lamaPinguinoArmadilloCtx);
        handlerRegistry.insertExact("/gatto/canguro", gattoCanguroEx);

        Assertions.assertEquals(lamaCtx, getHandler(handlerRegistry, "/lama"));
        Assertions.assertEquals(lamaCtx, getHandler(handlerRegistry, "/lama/alpaca"));
        Assertions.assertEquals(lamaPinguinoEx, getHandler(handlerRegistry, "/lama/pinguino"));
        Assertions.assertEquals(lamaPinguinoCtx, getHandler(handlerRegistry, "/lama/pinguino/altro"));
        Assertions.assertEquals(lamaPinguinoArmadilloCtx, getHandler(handlerRegistry, "/lama/pinguino/armadillo"));
        Assertions.assertNull(getHandler(handlerRegistry, "/gatto"));
    }

    @Test
    void testMoreFitting2() {
        Object o1 = new Object();
        Object o2 = new Object();
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();

        handlerRegistry.insertCtx("/a", o1);
        handlerRegistry.insertCtx("/a/b/c", o2);

        Assertions.assertEquals(o1, getHandler(handlerRegistry, "/a/b"));
    }

    @Test
    void testOverlapping() {
        final HandlerRegistry<Object> registry = new HandlerRegistry<>();
        Assertions.assertTrue(registry.insertExact("/lama/pinguino", new Object()));
        Assertions.assertTrue(registry.insertCtx("/lama/pinguino", new Object()));
        Assertions.assertFalse(registry.insertCtx("/lama/pinguino", new Object()));
        Assertions.assertFalse(registry.insertExact("/lama/pinguino", new Object()));



    }

}
