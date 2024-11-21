package com.simonebasile.http;

import com.simonebasile.http.unpub.ConnectionClosedBeforeRequestStartException;
import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.HttpInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HttpRequest<T> extends HttpMessage<T> {
    protected final HttpVersion version;
    protected final String method;
    protected final String resource;

    public HttpRequest(HttpRequest<?> source, T body) {
        this(source.method, source.resource, source.version, source.headers, body);
    }
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

    //TODO pattern
    public static <T> HttpRequest<T> parse(HttpInputStream his, BodyReader<T> bodyReader) throws IOException {

        String line;
        try {
            line = his.readLine();
        }catch (EOFException e){
            throw new ConnectionClosedBeforeRequestStartException();
        }
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
