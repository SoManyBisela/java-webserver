package com.simonebasile.http;

import com.simonebasile.http.unpub.ConnectionClosedBeforeRequestStartException;
import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.HttpInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Represents an HTTP request.
 */
public class HttpRequest<T> extends HttpMessage<T> {
    protected final HttpVersion version;
    protected final String method;
    protected final String resource;


    /**
     * Creates copy of the given HTTP request with a new body.
     *
     * @param source the request to copy
     * @param body the body of the request
     */
    public HttpRequest(HttpRequest<?> source, T body) {
        this(source.method, source.resource, source.version, source.headers, body);
    }

    /**
     * Creates a new HTTP request.
     *
     * @param method the HTTP method
     * @param resource the resource requested
     * @param version the HTTP version
     * @param headers the headers of the request
     * @param body the body of the request
     */
    public HttpRequest(String method, String resource, HttpVersion version, HttpHeaders headers, T body) {
        super(Objects.requireNonNull(headers), body);
        this.version = Objects.requireNonNull(version);
        this.method = Objects.requireNonNull(method);
        this.resource = Objects.requireNonNull(resource);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", resource='" + resource + '\'' +
                ", version=" + version +
                ", headers=" + headers +
                '}';
    }

    /**
     * Returns true if the request is a WebSocket connection request.
     *
     * @return true if the request is a WebSocket connection request
     */
    boolean isWebSocketConnection() {
        final List<String> connection = headers.connection();
        final String upgrade = headers.upgrade();

        return "GET".equalsIgnoreCase(method)
                && "websocket".equalsIgnoreCase(upgrade)
                && connection != null && connection.contains("upgrade");
    }

    public String getResource() {
        return resource;
    }

    public String getMethod() {
        return method;
    }

    public HttpVersion getVersion() {
        return version;
    }
}
