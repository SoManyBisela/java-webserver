package com.simonebasile.http;

import com.simonebasile.http.unpub.CustomException;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents the headers of an HTTP message.
 * The headers are case-insensitive and are stored in upper case.
 */
public class HttpHeaders {
    private final HashMap<String, List<String>> headers;

    public HttpHeaders() {
        this.headers = new HashMap<>();
    }

    public HttpHeaders(HttpHeaders other) {
        this.headers = new HashMap<>(other.headers);
    }

    private static String norm(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    /**
     * returns the value of the Content-length header as an Integer.
     * @return the value of the Content-length header as an Integer or null if the header is not present.
     */
    public Integer contentLength() {
        List<String> strings = get("Content-length");
        if (strings == null || strings.isEmpty()) {
            return null;
        } else if (strings.size() == 1) {
            try {
                return Integer.parseInt(strings.get(0));
            } catch (NumberFormatException e) {
                throw new CustomException("Invalid header: content-length is not a number", e);
            }
        } else {
            throw new CustomException("Invalid request: Multiple content-length headers");
        }
    }

    /**
     * Returns the value of the Upgrade header.
     * @return the value of the Content-type header or null if the header is not present.
     * @throws CustomException if the header is present multiple times.
     */
    public String upgrade() {
        return getExact("Upgrade");
    }

    /**
     * Returns the values of the Connection header.
     * @return values of the Connection header or null if the header is not present.
     * @throws CustomException if the header is present multiple times.
     */
    public List<String> connection() {
        final String connection = getExact("Connection");
        if(connection == null) return null;
        return Arrays.stream(connection.split(",")).map(String::trim).map(String::toLowerCase).toList();
    }

    /**
     * Returns the values of the Transfer-Encoding header.
     * @return values of the Connection header or null if the header is not present.
     * @throws CustomException if the header is present multiple times.
     */
    public List<String> transferEncoding() {
        List<String> strings = new ArrayList<>();
        final List<String> headers = get("Transfer-Encoding");
        if(headers == null) return strings;
        headers.stream().flatMap(a -> Stream.of(a.split(",")))
                .map(String::trim)
                .map(String::toLowerCase)
                .forEach(strings::add);
        return strings;
    }

    /**
     * returns the entries of the headers.
     * @return the entries of the headers.
     */
    public Iterable<Map.Entry<String, List<String>>> entries() {
        return headers.entrySet();
    }

    /**
     * Adds a header to the HttpHeaders.
     * @param key the name of the header
     * @param value the value of the header
     * @return this HttpHeaders
     */
    public HttpHeaders add(String key, String value) {
        headers.computeIfAbsent(norm(key), k -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * returns the values of the header with the given key.
     * @return a list of values of the header or null if the header is not present.
     */
    public List<String> get(String key) {
        return headers.get(norm(key));
    }

    /**
     * Returns the first value of the header with the given key.
     * @param key the name of the header
     * @return the first value of the header or null if the header is not present.
     */
    public String getFirst(String key) {
        List<String> vs = get(key);
        if(vs == null || vs.isEmpty()) return null;
        return vs.get(0);
    }

    /**
     * Returns the value of the header with the given key.
     * If the header is not present, null is returned.
     * If the header is present multiple times, an exception is thrown.
     * @param key the key of the header
     * @return the value of the header or null if the header is not present.
     * @throws CustomException if the header is present multiple times.
     */
    public String getExact(String key) {
        List<String> vs = get(key);
        if(vs == null || vs.isEmpty()) return null;
        if(vs.size() == 1) return vs.get(0);
        throw new CustomException("Multiple values for key: " + key);
    }

    /**
     * Sets a cookie in the HttpHeaders.
     * Cookies are set with the HttpOnly and SameSite=Strict attributes.
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    public void setCookie(String name, String value) {
        this.add("Set-Cookie", name + "=" + value + "; HttpOnly; SameSite=Strict");
    }

    /**
     * Returns the value of the cookie with the given name.
     * @param name the name of the cookie
     * @return the value of the cookie or null if the cookie is not present.
     */
    public String getCookie(String name) {
        List<String> cookieHeader = get("Cookie");
        if(cookieHeader == null || cookieHeader.isEmpty()) return null;
        return cookieHeader.stream()
                .flatMap(b -> Arrays.stream(b.split("; ")))
                .map(String::trim)
                .map(HttpHeaders::splitCookie)
                .filter(a -> name.equals(a[0]))
                .map(a -> a[1])
                .findFirst().orElse(null);
    }

    /**
     * Utility method to split a cookie in name and value.
     */
    private static String[] splitCookie(String cookie) {
        String[] res = new String[2];
        int i = cookie.indexOf("=");
        if(i == -1){
            res[0] = cookie;
            res[1] = "";
        } else {
            res[0] = cookie.substring(0, i);
            res[1] = cookie.substring(i + 1);
        }
        return res;
    }



}
