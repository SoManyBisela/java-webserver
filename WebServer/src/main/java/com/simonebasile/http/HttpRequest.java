package com.simonebasile.http;

import com.simonebasile.CustomException;

import java.io.IOException;
import java.util.List;

public class HttpRequest<T>{
    protected final String method;
    protected final String resource;
    protected final HttpVersion version;
    protected final HttpHeaders headers;
    protected final T body;

    public HttpRequest(String method, String resource, HttpVersion version, HttpHeaders headers, T body) {
        this.method = method;
        this.resource = resource;
        this.version = version;
        this.headers = headers;
        this.body = body;
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

    //TODO pattern
    public static <T> HttpRequest<T> parse(HttpInputStream his, BodyReader<T> bodyReader) throws IOException {
        String line = his.readLine();
        String[] statusLine = line.split(" ");
        if (statusLine.length != 3) {
            throw new CustomException("Invalid status line: " + line);
        }

        String method = statusLine[0]; //TODO parse
        String resource = statusLine[1];
        HttpVersion version = HttpVersion.parse(statusLine[2]);

        HttpHeaders headers = new HttpHeaders();
        while (!(line = his.readLine()).isEmpty()) {
            headers.parseLine(line);
        }
        Integer length = headers.contentLength();
        if(length == null) length = 0;
        T body = bodyReader.readBody(his, length);
        return new HttpRequest<>(method, resource, version, headers, body);
    }

    boolean isWebSocketConnection() {
        final List<String> connection = headers.connection();
        final String upgrade = headers.upgrade();

        return "GET".equalsIgnoreCase(method)
                && "websocket".equalsIgnoreCase(upgrade)
                && connection != null && connection.contains("UPGRADE");
    }

    public String getResource() {
        return resource;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }
}
