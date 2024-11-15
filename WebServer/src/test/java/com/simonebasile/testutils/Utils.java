package com.simonebasile.testutils;

import java.io.File;
import java.io.InputStream;

public class Utils {
    public static File fromResource(String r)  {
        return new File(Thread.currentThread().getContextClassLoader().getResource(r).getFile());
    }

    public static InputStream streamFromResource(String r)  {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(r);
    }
}
