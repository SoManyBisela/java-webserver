package com.simonebasile.http;

import com.simonebasile.http.handlers.StaticFileHandler;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.FileResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class StaticFileHandlerTest {

    private HttpRoutingContextImpl<InputStream, RequestContext> routingContext;
    private final String rootDirectory = "src/test/resources/fileHandlerRoot";

    @BeforeEach
    public void setUp() {
        routingContext = new HttpRoutingContextImpl<>();
        routingContext.registerHttpContext("/static", new StaticFileHandler(rootDirectory));
    }

    @Test
    public void testHandleGetFileExists() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/static/testfile.txt", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[0]));
        RequestContext context = new RequestContext();

        HttpResponse<? extends HttpResponse.ResponseBody> response = routingContext.handle(request, context);

        assertEquals(200, response.getStatusCode());
        assertInstanceOf(FileResponseBody.class, response.getBody());
    }

    @Test
    public void testHandleGetFileNotExists() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/static/nonexistentfile.txt", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[0]));
        RequestContext context = new RequestContext();

        HttpResponse<? extends HttpResponse.ResponseBody> response = routingContext.handle(request, context);

        assertEquals(404, response.getStatusCode());
        assertInstanceOf(ByteResponseBody.class, response.getBody());
    }

    @Test
    public void testHandleHeadFileExists() {
        HttpRequest<InputStream> request = new HttpRequest<>("HEAD", "/static/testfile.txt", HttpVersion.V1_1,  new HttpHeaders(), new ByteArrayInputStream(new byte[0]));
        RequestContext context = new RequestContext();

        HttpResponse<? extends HttpResponse.ResponseBody> response = routingContext.handle(request, context);

        assertEquals(200, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testHandleHeadFileNotExists() {
        HttpRequest<InputStream> request = new HttpRequest<>("HEAD", "/static/nonexistentfile.txt", HttpVersion.V1_1,new HttpHeaders(), new ByteArrayInputStream(new byte[0]));
        RequestContext context = new RequestContext();
        HttpResponse<? extends HttpResponse.ResponseBody> response = routingContext.handle(request, context);

        assertEquals(404, response.getStatusCode());
        assertNull(response.getBody());
    }

    private static String bodyToString(ByteResponseBody body) {
        var baos = new ByteArrayOutputStream();
        try {
            body.write(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }
}