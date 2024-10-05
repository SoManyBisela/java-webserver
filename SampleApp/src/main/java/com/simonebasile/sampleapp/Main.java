package com.simonebasile.sampleapp;

import com.simonebasile.http.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        WebServer webServer = new WebServer(10100);
        webServer.registerHttpHandler("/miao", (req, out) -> {
            String res = req.getResource();
            log.info("Received call at {}", res);
            try {
                out.writeStatus(req.getVersion(), 200, "Ok");
                out.writeHeader("Content-type", "text/html; charset=utf-8");
                out.writeBody("""
                    <!DOCTYPE HTML>
                    <html>
                    <head>
                    <title>testapp</title>
                    </head>
                    <body>
                    <h1>Welcome to the test app</h1>
                    <p>lorem ipsum etc</p>
                    </body>
                    </html>
                    """.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        webServer.registerWebSocketHandler("/wsh", ((req, client, his, hos) -> {
            log.info("Received websocket connection");

        }));
        webServer.start();
    }
}
