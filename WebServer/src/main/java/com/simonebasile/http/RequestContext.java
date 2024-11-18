package com.simonebasile.http;

public class RequestContext {
    private ResourceMatch contextMatch;

    public ResourceMatch getContextMatch() {
        return contextMatch;
    }

    public void setContextMatch(ResourceMatch contextMatch) {
        this.contextMatch = contextMatch;
    }
}
