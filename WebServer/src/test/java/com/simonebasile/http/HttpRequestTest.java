package com.simonebasile.http;

import com.simonebasile.http.unpub.ConnectionClosedBeforeRequestStartException;
import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.HttpInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        HttpRequest<String> parse = HttpRequest.parse(in, (a, l) -> {
            Assertions.assertEquals(0, l);
            return null;
        });

        Assertions.assertTrue(parse.isWebSocketConnection());
    }

    @Test
    public void testRequestParsing() throws IOException {
        String body = "123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V123456789V";
        String reqStr = "GET / HTTP/1.1\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 100\r\n" +
                        "\r\n" + body;
        HttpInputStream in = fromString(reqStr);
        HttpRequest<String> parse = HttpRequest.parse(in, (a, l) -> {
            Assertions.assertEquals(100, l);
            try {
                byte[] bytes = a.readAllBytes();
                return  new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println(parse);


        HttpRequest<String> copy = new HttpRequest<>(parse, parse.getBody());
        Assertions.assertEquals("GET", copy.getMethod());
        Assertions.assertEquals("/", copy.getResource());
        Assertions.assertEquals(HttpVersion.V1_1, copy.getVersion());
        Assertions.assertEquals("text/html", copy.getHeaders().getExact("content-type"));
        Assertions.assertEquals(body, copy.getBody());
        Assertions.assertFalse(copy.isWebSocketConnection());
    }

    @Test
    public void testParseFailures() throws IOException {
        HttpInputStream truncatedIs = fromString("GET / HTTP");
        try {
            HttpRequest.parse(truncatedIs, (a, b) ->null);
        } catch (Exception e) {
            Assertions.assertInstanceOf(ConnectionClosedBeforeRequestStartException.class, e);
        }

        HttpInputStream truncStat = fromString("GET / HTTP/1.1\r\nheader: va");
        try {
            HttpRequest.parse(truncStat, (a, b) ->null);
        } catch (Exception e) {
            Assertions.assertInstanceOf(EOFException.class, e);
        }


        HttpInputStream versionless = fromString("GET /\r\n");
        try {
            HttpRequest.parse(versionless, (a, b) ->null);
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        HttpInputStream invalidVersion = fromString("GET / HTP/1.1\r\n");
        try {
            HttpRequest.parse(invalidVersion, (a, b) ->null);
        } catch (Exception e) {
            Assertions.assertInstanceOf(IllegalArgumentException.class, e);
        }

    }

    private HttpInputStream fromString(String str) {
        return new HttpInputStream(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));
    }
}
