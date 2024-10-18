package com.simonebasile.sampleapp;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class SeCuRiTyInTeRcEpToR_XD implements HttpInterceptor<InputStream> {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<InputStream> request, HttpRequestHandler<InputStream> next) {
        log.info("Received request at {}", request.getResource());
        if(!"Miao".equals(request.getHeaders().getFirst("X-Super-Secure-Header"))) {
            return new HttpResponse<>(request.getVersion(), 403, new HttpHeaders(), new ByteResponseBody("Forbidden"));
        }
        final HttpResponse<? extends HttpResponse.ResponseBody> handle = next.handle(request);
        log.info("Service finished for request at {}", request.getResource());
        return handle;
    }
}

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        WebServer webServer = new WebServer(10100);
        webServer.registerHttpContext("/miao", req -> handle(req, "miao context"));
        webServer.registerHttpHandler("/miao", req -> handle(req, "miao handler"));
        webServer.registerWebSocketHandler("/wsh", ((s) -> log.info("Received websocket connection")));
        webServer.registerInterceptor(new SeCuRiTyInTeRcEpToR_XD());
        webServer.start();
    }

    private static HttpResponse<HttpResponse.ResponseBody> handle(HttpRequest<InputStream> req, String where) {
        String res = req.getResource();
        log.info("Received call at {}", res);
        String response = """
                <!DOCTYPE HTML>
                <html>
                <head>
                <title>testapp</title>
                </head>
                <body>
                <h1>Welcome to the test app</h1>
                <p>you are inside""" + " " + where + """
                </p>
                </body>
                </html>
                """;
        return new HttpResponse<>(
                req.getVersion(), 200,
                new HttpHeaders(),
                new ByteResponseBody(response)
        );
    }
}
