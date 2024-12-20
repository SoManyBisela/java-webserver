package com.simonebasile.http;

import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.internal.HttpMessageUtils;
import com.simonebasile.http.internal.HttpOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseTests {

    @Test
    public void testResponse() throws IOException {
        HttpResponse<ByteResponseBody> response = new HttpResponse<>(new ByteResponseBody("Response"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpMessageUtils.writeResponse(HttpVersion.V1_1, response, new HttpOutputStream(out));
        Assertions.assertEquals("""
                HTTP/1.1 200 OK\r
                CONTENT-TYPE: text/plain; charset=UTF-8\r
                CONTENT-LENGTH: 8\r
                \r
                Response""",out.toString());

    }

    @Test
    public void testResponseNoBody() throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Prova", "header");
        HttpResponse<ByteResponseBody> response = new HttpResponse<>(200, httpHeaders, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpMessageUtils.writeResponse(HttpVersion.V1_1, response, new HttpOutputStream(out));
        Assertions.assertEquals("""
                HTTP/1.1 200 OK\r
                CONTENT-LENGTH: 0\r
                PROVA: header\r
                \r
                """, out.toString());

    }

    @Test
    public void testResponseStreamBody() throws IOException {
        HttpResponse<HttpResponseBody> response = new HttpResponse<>(new HttpResponseBody() {

            @Override
            public void write(OutputStream out) throws IOException {
                out.write("Response ".getBytes());
                out.write("Completed".getBytes());
            }

            @Override
            public Long contentLength() {
                return null;
            }

            @Override
            public String contentType() {
                return "text/plain";
            }
        });
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpMessageUtils.writeResponse(HttpVersion.V1_1, response, new HttpOutputStream(out));
        Assertions.assertEquals("""
                    HTTP/1.1 200 OK\r
                    TRANSFER-ENCODING: chunked\r
                    CONTENT-TYPE: text/plain\r
                    \r
                    12\r
                    Response Completed\r
                    0\r
                    \r
                    """,out.toString());

    }
}
