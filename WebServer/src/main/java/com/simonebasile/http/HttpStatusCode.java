package com.simonebasile.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods to get the status string of an HTTP status code.
 */
public class HttpStatusCode {
    private static final Logger log = LoggerFactory.getLogger(HttpStatusCode.class);

    public static String getStatusString(int statusCode) {
        return switch (statusCode) {
            case 101 -> "Switching Protocols";
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 418 -> "I'm a teapot";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> {
                log.warn("Missing status string for code {}", statusCode);
                yield "";
            }
        };
    }
}
