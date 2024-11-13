package com.simonebasile.testutils;

import java.io.File;

public class Utils {
    public static File fromResource(String r)  {
        return new File(Thread.currentThread().getContextClassLoader().getResource(r).getFile());
    }

}
