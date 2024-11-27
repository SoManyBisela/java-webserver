package com.simonebasile.http;

import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.http.internal.ConnectionClosedBeforeRequestStartException;
import com.simonebasile.http.internal.CustomException;
import com.simonebasile.http.internal.HttpInputStream;
import com.simonebasile.http.internal.HttpMessageUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestTest {

    @Test
    public void testWsUpdate() throws IOException {
        String reqStr = """
                GET / HTTP/1.1\r
                Connection: upgrade\r
                Upgrade: websocket\r
                \r
                """;
        HttpInputStream in = fromString(reqStr);
        HttpRequest<InputStream> request = HttpMessageUtils.parseRequest(in);
        assertEquals(0, request.getBody().readAllBytes().length);

        Assertions.assertTrue(request.isWebSocketConnection());
    }

    @Test
    public void testRequestParsing() throws IOException {
        String body = "123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V";
        String reqStr = "GET / HTTP/1.1\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 100\r\n" +
                        "\r\n" + body;
        HttpInputStream in = fromString(reqStr);
        HttpRequest<InputStream> parse = HttpMessageUtils.parseRequest(in);
        byte[] parsedBody = parse.getBody().readAllBytes();
        String stringBody = new String(parsedBody, StandardCharsets.UTF_8);
        assertEquals(parsedBody.length, 100);
        assertEquals(body, stringBody);



        HttpRequest<String> copy = new HttpRequest<>(parse, stringBody);
        assertEquals("GET", copy.getMethod());
        assertEquals("/", copy.getResource());
        assertEquals(HttpVersion.V1_1, copy.getVersion());
        assertEquals("text/html", copy.getHeaders().getExact("content-type"));
        assertEquals(body, copy.getBody());
        Assertions.assertFalse(copy.isWebSocketConnection());
    }

    @Test
    public void testParseFailures() throws IOException {
        HttpInputStream truncatedIs = fromString("GET / HTTP");
        try {
            HttpMessageUtils.parseRequest(truncatedIs);
        } catch (Exception e) {
            Assertions.assertInstanceOf(ConnectionClosedBeforeRequestStartException.class, e);
        }

        HttpInputStream truncStat = fromString("GET / HTTP/1.1\r\nheader: va");
        try {
            HttpMessageUtils.parseRequest(truncStat);
        } catch (Exception e) {
            Assertions.assertInstanceOf(EOFException.class, e);
        }


        HttpInputStream versionless = fromString("GET /\r\n");
        try {
            HttpMessageUtils.parseRequest(versionless);
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        HttpInputStream invalidVersion = fromString("GET / HTP/1.1\r\n");
        try {
            HttpMessageUtils.parseRequest(invalidVersion);
        } catch (Exception e) {
            Assertions.assertInstanceOf(IllegalArgumentException.class, e);
        }

    }

    private HttpInputStream fromString(String str) {
        return new HttpInputStream(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));
    }
}
