package com.simonebasile.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryTest {

    @Test
    void testContext() {
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();
        Object o = new Object();
        handlerRegistry.insertCtx("/lama", o);
        Assertions.assertEquals(o, handlerRegistry.getHandler("/lama"));
        Assertions.assertEquals(o, handlerRegistry.getHandler("/lama/cane"));
        Assertions.assertEquals(o, handlerRegistry.getHandler("/lama/costruzione"));
        Assertions.assertEquals(o, handlerRegistry.getHandler("/lama/altroPathLungo"));
        Assertions.assertNull(handlerRegistry.getHandler("/alpaca"));
    }

    @Test
    void testExact() {
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();
        Object o = new Object();
        handlerRegistry.insertExact("/lama/pinguino", o);
        Assertions.assertEquals(o, handlerRegistry.getHandler("/lama/pinguino"));
        Assertions.assertNull(handlerRegistry.getHandler("/lama"));
        Assertions.assertNull(handlerRegistry.getHandler("/lama/cane"));
        Assertions.assertNull(handlerRegistry.getHandler("/lama/costruzione"));
        Assertions.assertNull(handlerRegistry.getHandler("/lama/altroPathLungo"));
        Assertions.assertNull(handlerRegistry.getHandler("/alpaca"));
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

        Assertions.assertEquals(lamaCtx, handlerRegistry.getHandler("/lama"));
        Assertions.assertEquals(lamaCtx, handlerRegistry.getHandler("/lama/alpaca"));
        Assertions.assertEquals(lamaPinguinoEx, handlerRegistry.getHandler("/lama/pinguino"));
        Assertions.assertEquals(lamaPinguinoCtx, handlerRegistry.getHandler("/lama/pinguino/altro"));
        Assertions.assertEquals(lamaPinguinoArmadilloCtx, handlerRegistry.getHandler("/lama/pinguino/armadillo"));
        Assertions.assertNull(handlerRegistry.getHandler("/gatto"));
    }

    @Test
    void testMoreFitting2() {
        Object o1 = new Object();
        Object o2 = new Object();
        HandlerRegistry<Object> handlerRegistry = new HandlerRegistry<>();

        handlerRegistry.insertCtx("/a", o1);
        handlerRegistry.insertCtx("/a/b/c", o2);

        Assertions.assertEquals(o1, handlerRegistry.getHandler("/a/b"));
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
