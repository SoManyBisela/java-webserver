package com.simonebasile.http;

public enum HttpVersion {
    V1_1("HTTP/1.1");

    HttpVersion(String value) {
        this.value = value;
    }

    public final String value;

    public static HttpVersion parse(String version) {
        return switch (version.toUpperCase()) {
            case "HTTP/1.1" -> V1_1;
            default -> throw new IllegalArgumentException("Unknown HTTP version: " + version);
        };

    }
}
