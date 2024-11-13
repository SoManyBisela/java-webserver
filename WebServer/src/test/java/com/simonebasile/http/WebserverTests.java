package com.simonebasile.http;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.unpub.HttpInputStream;
import com.simonebasile.http.unpub.HttpOutputStream;
import com.simonebasile.http.unpub.WebSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static com.simonebasile.testutils.Utils.fromResource;
import static org.junit.jupiter.api.Assertions.*;

public class WebserverTests {

    @Test
    public void testWebserver() throws IOException, InterruptedException {
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        boolean[] called = new boolean[1];
        webServer.registerHttpContext("/", (r) -> {
            called[0] = true;
            return new HttpResponse<>(r.getVersion(), 200, new HttpHeaders(), new ByteResponseBody("<html><body><h1>Ciao</h1></body></html>", "text/html"));
        });
        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> webServer.start(semaphore::release));
        thread.start();
        semaphore.acquire();
        try (Socket c = new Socket("localhost", port)) {
            String request = """
                    GET /index.html HTTP/1.1\r
                    Accept: text/html\r
                    \r
                    """;
            c.getOutputStream().write(request.getBytes());
        }
        webServer.stop();
        thread.join();
        Assertions.assertTrue(called[0]);
    }

    @Test
    public void testWebsocket() throws IOException, InterruptedException {
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        boolean[] error = new boolean[1];
        boolean[] written = new boolean[1];
        boolean[] closed = new boolean[1];
        boolean[] correctType = new boolean[1];

        String[] messages = {
                "Handshake Completato",
                genStr("Messaggio ripetuto!", 200),
                genStr("Messaggio ripetuto!", 1 << 16),
        };

        int[] rcvMsg = new int[1];
        String[] rcvd = new String[messages.length];

        webServer.registerWebSocketContext("/", new NewWsHandler<>() {
            @Override
            public Object newContext() {
                return new Object();
            }

            @Override
            public HandshakeResult onServiceHandshake(String[] availableService, Object o) {
                if(availableService.length == 0) {
                    return HandshakeResult.accept("default");
                } else {
                    return HandshakeResult.accept(availableService[0]);
                }
            }

            @Override
            public void onHandshakeComplete(WebsocketWriterImpl websocketWriter, Object ctx) {
                try {
                    websocketWriter.sendText("Handshake Completato");
                    written[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    error[0] = true;
                }
            }

            @Override
            public void onMessage(WebsocketMessage msg, Object o) {
                correctType[0] = msg.type == WebsocketMessage.MsgType.TEXT;
                String s = new String(msg.data[0]);
                int i = rcvMsg[0]++;
                rcvd[i] = s;
            }

            @Override
            public void onClose(Object o) {
                closed[0] = true;

            }
        });

        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> webServer.start(semaphore::release));
        thread.start();
        semaphore.acquire();

        try(Socket c = new Socket("localhost", port)) {
            HttpInputStream is = new HttpInputStream(c.getInputStream());
            HttpOutputStream os = new HttpOutputStream(c.getOutputStream());
            os.write("GET /index.html HTTP/1.1\r\n");
            os.writeHeader("Connection", "upgrade");
            os.writeHeader("Upgrade", "websocket");
            os.endHeaders();
            os.flush();
            while(!is.readLine().isEmpty()) {}
            WebSocket webSocket = new WebSocket(c, is, os);
            for(String msg: messages) {
                webSocket.maskAndSendDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_TEXT, msg.getBytes(StandardCharsets.UTF_8));
            }
            WebSocket.WSDataFrame dataFrame = webSocket.getDataFrame();
            assertFalse(dataFrame.masked);
            assertEquals(WebSocket.WSDataFrame.FIN, dataFrame.flags);
            assertEquals("Handshake Completato", new String(dataFrame.body.readAllBytes()));
        }

        webServer.stop();
        thread.join();

        Assertions.assertFalse(error[0]);
        Assertions.assertTrue(written[0]);
        Assertions.assertTrue(closed[0]);
        Assertions.assertArrayEquals(messages, rcvd);
        Assertions.assertTrue(correctType[0]);

    }

    private String genStr(String s, int len) {
        StringBuilder stringBuilder = new StringBuilder(len);
        for(int i = 0; i < len; i++) {
            stringBuilder.append(s.charAt(i % s.length()));
        }
        return stringBuilder.toString();
    }


}
