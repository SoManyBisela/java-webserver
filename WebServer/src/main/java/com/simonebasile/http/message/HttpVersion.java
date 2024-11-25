package com.simonebasile.http.message;

/**
 * This enum represents the version of the HTTP protocol.
 */
public enum HttpVersion {
    V1_1("HTTP/1.1");

    HttpVersion(String value) {
        this.value = value;
    }

    public final String value;

    /**
     * Parses an HTTP version from a string.
     *
     * @param version the string to parse
     * @return the parsed HTTP version
     * @throws IllegalArgumentException if the version is not recognized
     */
    public static HttpVersion parse(String version) {
        return switch (version.toUpperCase()) {
            case "HTTP/1.1" -> V1_1;
            default -> throw new IllegalArgumentException("Unknown HTTP version: " + version);
        };

    }
}
