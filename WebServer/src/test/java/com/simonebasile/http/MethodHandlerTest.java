package com.simonebasile.http;

import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MethodHandlerTest {

    class TestMethodHandler extends MethodHandler<String, Object> {
        @Override
        protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends String> r, Object context) {
            return new HttpResponse<>(200, new HttpHeaders(), new ByteResponseBody("GET response"));
        }

        @Override
        protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends String> r, Object context) {
            return new HttpResponse<>(201, new HttpHeaders(), new ByteResponseBody("POST response"));
        }

        @Override
        protected HttpResponse<? extends HttpResponseBody> handlePut(HttpRequest<? extends String> r, Object context) {
            return new HttpResponse<>(202, new HttpHeaders(), new ByteResponseBody("PUT response"));
        }

        @Override
        protected HttpResponse<? extends HttpResponseBody> handleDelete(HttpRequest<? extends String> r, Object context) {
            return new HttpResponse<>(204, new HttpHeaders(), new ByteResponseBody("DELETE response"));
        }
    }


    @Test
    public void testHandleGet() {
        TestMethodHandler handler = new TestMethodHandler();
        HttpRequest<String> request = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(),  "body");
        HttpResponse<? extends HttpResponseBody> response = handler.handle(request, new Object());
        assertEquals(200, response.getStatusCode());
        assertEquals("GET response", bodyToString((ByteResponseBody) response.getBody()));
    }

    @Test
    public void testHandlePost() {
        TestMethodHandler handler = new TestMethodHandler();
        HttpRequest<String> request = new HttpRequest<>("POST", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> response = handler.handle(request, new Object());
        assertEquals(201, response.getStatusCode());
        assertEquals("POST response", bodyToString((ByteResponseBody) response.getBody()));
    }

    @Test
    public void testHandlePut() {
        TestMethodHandler handler = new TestMethodHandler();
        HttpRequest<String> request = new HttpRequest<>("PUT", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> response = handler.handle(request, new Object());
        assertEquals(202, response.getStatusCode());
        assertEquals("PUT response", bodyToString((ByteResponseBody) response.getBody()));
    }

    @Test
    public void testHandleDelete() {
        TestMethodHandler handler = new TestMethodHandler();
        HttpRequest<String> request = new HttpRequest<>("DELETE", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> response = handler.handle(request, new Object());
        assertEquals(204, response.getStatusCode());
        assertEquals("DELETE response", bodyToString((ByteResponseBody) response.getBody()));
    }

    @Test
    public void testHandleMethodNotAllowed() {
        var handler = new MethodHandler<>();

        HttpRequest<String> requestOptions = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> responseOptions = handler.handle(requestOptions, new Object());
        assertEquals(405, responseOptions.getStatusCode());
        assertEquals("method not allowed", bodyToString((ByteResponseBody) responseOptions.getBody()));

        HttpRequest<String> requestPatch = new HttpRequest<>("POST", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> responsePatch = handler.handle(requestPatch, new Object());
        assertEquals(405, responsePatch.getStatusCode());
        assertEquals("method not allowed", bodyToString((ByteResponseBody) responsePatch.getBody()));

        HttpRequest<String> requestTrace = new HttpRequest<>("PUT", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> responseTrace = handler.handle(requestTrace, new Object());
        assertEquals(405, responseTrace.getStatusCode());
        assertEquals("method not allowed", bodyToString((ByteResponseBody) responseTrace.getBody()));

        HttpRequest<String> requestConnect = new HttpRequest<>("DELETE", "/", HttpVersion.V1_1, new HttpHeaders(), "body");
        HttpResponse<? extends HttpResponseBody> responseConnect = handler.handle(requestConnect, new Object());
        assertEquals(405, responseConnect.getStatusCode());
        assertEquals("method not allowed", bodyToString((ByteResponseBody) responseConnect.getBody()));
    }

    public static String bodyToString(ByteResponseBody body) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            body.write(byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toString();
    }

}