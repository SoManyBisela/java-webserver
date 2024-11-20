package com.simonebasile.http;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRoutingContextTest {

    @Test
    public void testNestedRoutingContext() {
        HttpRoutingContext<String, RequestContext> rootContext = new HttpRoutingContext<>();
        HttpRoutingContext<String, RequestContext> nestedContext = new HttpRoutingContext<>();

        nestedContext.registerHttpHandler("/nested", (req, ctx) -> 
            new HttpResponse<>(200, new HttpHeaders(), new ByteResponseBody("Nested handler response"))
        );

        rootContext.registerHttpContext("/root", nestedContext);

        HttpRequest<String> request = new HttpRequest<>("GET", "/root/nested", HttpVersion.V1_1, new HttpHeaders(), "body");
        RequestContext requestContext = new RequestContext();
        HttpResponse<? extends HttpResponse.ResponseBody> response = rootContext.handle(request, requestContext);

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