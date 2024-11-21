package com.simonebasile.http;

/**
 * This class represents the context of an HTTP request.
 * It can be overridden by application developers to store additional information about the request.
 */
public class RequestContext {
    private ResourceMatch contextMatch;

    public ResourceMatch getContextMatch() {
        return contextMatch;
    }

    public void setContextMatch(ResourceMatch contextMatch) {
        this.contextMatch = contextMatch;
    }
}
