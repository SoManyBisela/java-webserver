package com.simonebasile.http;

import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.HttpInputStream;
import com.simonebasile.http.unpub.HttpOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpStreamsTest {

    @Test
    public void inputStreamTest() throws IOException {
        HttpInputStream httpInputStream = isFromString("Linea 1\r\nLinea 2\r\n");
        Assertions.assertEquals("Linea 1", httpInputStream.readLine());
        Assertions.assertEquals("Linea 2", httpInputStream.readLine());
    }

    @Test
    public void inputStreamTest2() throws IOException {
        HttpInputStream httpInputStream = isFromString("Linea 1\r\nLinea ");
        Assertions.assertEquals("Linea 1", httpInputStream.readLine());
        try {
            httpInputStream.readLine();
            Assertions.fail("Should have thrown");
        } catch (Exception e) {
            Assertions.assertInstanceOf(EOFException.class, e);
        }
    }

    @Test
    public void inputStreamTest3() throws IOException {
        HttpInputStream httpInputStream = isFromString("Linea 1\r\nLinea \r");
        Assertions.assertEquals("Linea 1", httpInputStream.readLine());
        try {
            httpInputStream.readLine();
            Assertions.fail("Should have thrown");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }
    }

    private HttpInputStream isFromString(String s) {
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return new HttpInputStream(in);
    }

    @Test
    public void outputStreamTest() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpOutputStream httpOutputStream = new HttpOutputStream(out);
        httpOutputStream.write("Simone");
        httpOutputStream.flush();
        Assertions.assertEquals("Simone", out.toString(StandardCharsets.UTF_8));
        out.reset();
        httpOutputStream.writeStatus(HttpVersion.V1_1, 200, "Ok");
        httpOutputStream.writeHeader("Content-type", "application/json");
        httpOutputStream.writeBody("miao".getBytes());
        httpOutputStream.flush();
        Assertions.assertEquals("HTTP/1.1 200 Ok\r\nContent-type: application/json\r\nContent-Length: 4\r\n\r\nmiao", out.toString(StandardCharsets.UTF_8));
        out.reset();
        httpOutputStream.writeStatus(HttpVersion.V1_1, 200, "Ok");
        httpOutputStream.writeHeader("Content-type", "application/json");
        httpOutputStream.endHeaders();
        httpOutputStream.flush();
        Assertions.assertEquals("HTTP/1.1 200 Ok\r\nContent-type: application/json\r\n\r\n", out.toString(StandardCharsets.UTF_8));
    }

}
