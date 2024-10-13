package com.simonebasile.http;

import com.simonebasile.CustomException;

import java.util.*;

public class HttpHeaders {
    private final HashMap<String, List<String>> headers;

    public HttpHeaders() {
        this.headers = new HashMap<>();
    }

    HttpHeaders(HttpHeaders other) {
        this.headers = new HashMap<>(other.headers);
    }

    private static String norm(String s) {
        return s.toUpperCase(Locale.ROOT);
    }

    public void add(String key, String value) {
        headers.computeIfAbsent(norm(key), k -> new ArrayList<>()).add(value);
    }

    public List<String> get(String key) {
        return headers.get(norm(key));
    }

    public String getFirst(String key) {
        List<String> vs = get(key);
        if(vs == null || vs.isEmpty()) return null;
        return vs.get(0);
    }

    public String getExact(String key) {
        List<String> vs = get(key);
        if(vs == null || vs.isEmpty()) return null;
        if(vs.size() == 1) return vs.get(0);
        throw new CustomException("Multiple values for key: " + key);
    }

    void parseLine(String line) {
        int length = line.length();
        int colon = line.indexOf(":");
        String err = null;
        if (colon == -1) {
            err = "Invalid header: Missing colon";
        } else if (length == colon + 1) {
            err = "Invalid header: Ends with colon";
        }
        if (err != null) throw new CustomException(err);
        String key = line.substring(0, colon);
        while (++colon < length &&
                Character.isWhitespace(line.charAt(colon))) {
        }
        if (colon == length) throw new CustomException("Invalid header: Missing value");
        String value = line.substring(colon);
        add(key, value);
    }

    Integer contentLength() {
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

    String upgrade() {
        return getExact("Upgrade");
    }

    List<String> connection() {
        final String connection = getExact("Connection");
        if(connection == null) return null;
        return Arrays.stream(connection.split(",")).map(String::trim).map(String::toUpperCase).toList();
    }

    Iterable<Map.Entry<String, List<String>>> entries() {
        return headers.entrySet();
    }

}
