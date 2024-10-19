package com.simonebasile.sampleapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DebugRegistry {
    private static final HashMap<String, List<Object>> dbgbeans = new HashMap<>();
    public static void add(String s, Object o) {
        dbgbeans.computeIfAbsent(s, k -> new ArrayList<>()).add(o);
    }
}
