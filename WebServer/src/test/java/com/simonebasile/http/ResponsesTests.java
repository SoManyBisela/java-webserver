package com.simonebasile.http;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.FileResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.simonebasile.testutils.Utils.fromResource;

public class ResponsesTests {

    @Test
    void testFileResponseBody() throws IOException {
        File test = fromResource("responseBodies/test");

        FileResponseBody rawTest = new FileResponseBody(test);
        FileResponseBody textTest = new FileResponseBody(fromResource("responseBodies/test.txt"));
        FileResponseBody jsTest = new FileResponseBody(fromResource("responseBodies/test.js"));
        FileResponseBody htmlTest = new FileResponseBody(fromResource("responseBodies/test.html"));
        FileResponseBody xmlTest = new FileResponseBody(fromResource("responseBodies/test.xml"));
        FileResponseBody cssTest = new FileResponseBody(fromResource("responseBodies/test.css"));
        FileResponseBody jsonTest = new FileResponseBody(fromResource("responseBodies/test.json"));
        FileResponseBody customTest = new FileResponseBody(fromResource("responseBodies/test.custom"));

        Assertions.assertEquals("application/octet-stream", rawTest.contentType());
        Assertions.assertEquals("application/octet-stream", customTest.contentType());
        Assertions.assertEquals("text/javascript", jsTest.contentType());
        Assertions.assertEquals("text/html", htmlTest.contentType());
        Assertions.assertEquals("text/xml", xmlTest.contentType());
        Assertions.assertEquals("text/css", cssTest.contentType());
        Assertions.assertEquals("application/json", jsonTest.contentType());
        Assertions.assertEquals("text/plain", textTest.contentType());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rawTest.write(baos);
        var fromFile = Files.readAllBytes(test.toPath());
        var fromResponseBody = baos.toByteArray();
        Assertions.assertArrayEquals(fromFile, fromResponseBody);

        Assertions.assertEquals(fromFile.length, rawTest.contentLength());
        Assertions.assertEquals(1024 * 16, rawTest.contentLength());
    }

    @Test
    void testByteResponseBody() throws IOException {
        String content = "Prova";
        ByteResponseBody byteResponseBody = new ByteResponseBody(content, StandardCharsets.UTF_8, "text/plain");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byteResponseBody.write(baos);
        Assertions.assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), baos.toByteArray());
        Assertions.assertEquals("text/plain", byteResponseBody.contentType());
        Assertions.assertEquals(content.length(), byteResponseBody.contentLength());


        byteResponseBody = new ByteResponseBody(content, "text/plain");
        byteResponseBody = new ByteResponseBody(content);
        byteResponseBody = new ByteResponseBody(content.getBytes());
        byteResponseBody = new ByteResponseBody(content.getBytes(), "text/css");
    }


}
