package com.simonebasile.http;

import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.routing.HttpRoutingContextImpl;
import com.simonebasile.http.server.RequestContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRoutingContextTest {

    @Test
    public void testNestedRoutingContext() {
        HttpRoutingContextImpl<String, RequestContext> rootContext = new HttpRoutingContextImpl<>();
        HttpRoutingContextImpl<String, RequestContext> nestedContext = new HttpRoutingContextImpl<>();

        nestedContext.registerHttpHandler("/nested", (req, ctx) -> 
            new HttpResponse<>(200, new HttpHeaders(), new ByteResponseBody("Nested handler response"))
        );

        rootContext.registerHttpContext("/root", nestedContext);

        HttpRequest<String> request = new HttpRequest<>("GET", "/root/nested", HttpVersion.V1_1, new HttpHeaders(), "body");
        RequestContext requestContext = new RequestContext();
        HttpResponse<? extends HttpResponseBody> response = rootContext.handle(request, requestContext);

        assertEquals(200, response.getStatusCode());
        assertEquals("Nested handler response", bodyToString((ByteResponseBody) response.getBody()));
    }

    private String bodyToString(ByteResponseBody body) {
        var baos = new ByteArrayOutputStream();
        try {
            body.write(baos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return baos.toString();

    }
}