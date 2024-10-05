package com.simonebasile.http;

import java.util.*;

/**
 * Handler registry holds the registered handlers.
 */
class HandlerRegistry<T> {
    private final TreeMap<String, T> handlers;

    public HandlerRegistry() {
        this.handlers = new TreeMap<>();
    }

    /**
     * Registers the handler under the selected registration path
     *
     * @param path    the registration path
     * @param handler the handler to register
     */
    public boolean register(String path, T handler) {
        return handlers.putIfAbsent(path, handler) == null;
    }

    /**
     * Searches for a handler matching the input path.
     * Returns a handler registered
     * or null if no registered handler matches
     * If the registry is not indexed it gets indexed first
     *
     * @param path the path to match
     * @return a handler registered under a path that is either equal or a prefix of the input path
     *          or null if no registered handler matched
     */
    public T get(String path) {
        final Map.Entry<String, T> handlerentry = handlers.floorEntry(path);
        if(handlerentry == null) return null;
        String entryKey = handlerentry.getKey();
        if(path.startsWith(entryKey)) {
            return handlerentry.getValue();
        } else {
            return null;
        }
    }

}
