package com.simonebasile.http;

import com.simonebasile.http.unpub.CustomException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class HttpHeadersTests {
    @Test
    public void testHeaders() {
        HttpHeaders original = new HttpHeaders();
        original.parseLine("Content-type: application/json");
        original.add("ACCEPT", "text/xml");
        original.add("content-length", "16");
        original.add("upgrade", "websocket");
        original.add("connection", "keep-alive, transfer-encoding");
        original.setCookie("pin", "guino");
        original.add("Cookie", "ciro");
        original.add("Cookie", "lama=ntino; costi=tuzione");
        Iterable<Map.Entry<String, List<String>>> entries = original.entries();

        HttpHeaders copy = new HttpHeaders(original);

        List<String> connection = copy.connection();
        Assertions.assertTrue(connection.contains("keep-alive"));
        Assertions.assertTrue(connection.contains("transfer-encoding"));

        Assertions.assertEquals(16, copy.contentLength());
        Assertions.assertEquals("websocket", copy.upgrade());
        Assertions.assertEquals("tuzione", copy.getCookie("costi"));
        Assertions.assertEquals("application/json", copy.getFirst("content-type"));
        Assertions.assertEquals("text/xml", copy.get("accept").get(0));
        Assertions.assertEquals("ntino", copy.getCookie("lama"));
    }

    @Test
    public void testHeaderErrors() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Double-header", "first");
        httpHeaders.add("Double-header", "second");
        Assertions.assertNull(httpHeaders.contentLength());
        httpHeaders.add("content-length", "miao");

        try {
            httpHeaders.getExact("Double-header");
            Assertions.fail("Should throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        try {
            httpHeaders.contentLength();
            Assertions.fail("Should throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        httpHeaders.add("content-length", "2");
        try {
            httpHeaders.contentLength();
            Assertions.fail("Should throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        try {
            httpHeaders.parseLine("Lama:");
            Assertions.fail("Should throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

        try {
            httpHeaders.parseLine("Lama");
            Assertions.fail("Should throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }

    }
}
