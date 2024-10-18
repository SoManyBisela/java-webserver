package com.simonebasile.sampleapp.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArgonTest {
    @Test
    public void testArgonMatch() {
        String password = "password";
        Assertions.assertTrue(ArgonUtils.verify(password, ArgonUtils.hash(password)));
    }

    @Test
    public void testArgonNoMatch() {
        String password = "password";
        Assertions.assertFalse(ArgonUtils.verify("other password", ArgonUtils.hash(password)));
    }
}
