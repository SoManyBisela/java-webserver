package com.simonebasile.sampleapp;

import com.simonebasile.http.HttpOutputStream;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        WebServer webServer = new WebServer(10100);
        webServer.registerHttpContext("/miao", (req, out) -> handle(req, out, "miao context"));
        webServer.registerHttpHandler("/miao", (req, out) -> handle(req, out, "miao handler"));
        webServer.registerWebSocketHandler("/wsh", ((s) -> {
            log.info("Received websocket connection");

        }));
        webServer.start();
    }

    private static void handle(HttpRequest<InputStream> req, HttpOutputStream out, String where) {
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
                <p>you are inside """ + where + """
                </p>
                </body>
                </html>
                """;
        try {
            out.writeStatus(req.getVersion(), 200, "Ok");
            out.writeHeader("Content-type", "text/html; charset=utf-8");
            out.writeBody(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
