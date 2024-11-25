package com.simonebasile.http;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.unexported.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

public class WebserverTests {

    @Test
    public void testWebserverHttp() throws IOException, InterruptedException {
        var webServer = WebServer.builder().build();
        boolean[] ctxCalled = new boolean[1];
        boolean[] handlerCalled = new boolean[1];
        boolean[] interceptorCalled = new boolean[1];
        webServer.registerInterceptor((a, c, n) -> {
            interceptorCalled[0] = true;
            a.getHeaders().add("Intercepted", "true");
            return n.handle(a, c);
        });
        webServer.registerHttpHandler("/", (r, c) -> {
            handlerCalled[0] = true;
            Assertions.assertEquals("true", r.getHeaders().getFirst("Intercepted"));
            return new HttpResponse<>(200, new ByteResponseBody("resp"));
        });
        webServer.registerHttpContext("/", (r, c) -> {
            ctxCalled[0] = true;
            Assertions.assertEquals("true", r.getHeaders().getFirst("Intercepted"));
            return new HttpResponse<>(200, new HttpHeaders(), new ByteResponseBody("<html><body><h1>Ciao</h1></body></html>", "text/html"));
        });
        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> webServer.start(semaphore::release));
        thread.start();
        semaphore.acquire();
        try (Socket c = new Socket("localhost", webServer.getPort())) {
            String request = """
                    GET /index.html HTTP/1.1\r
                    Accept: text/html\r
                    \r
                    """;
            c.getOutputStream().write(request.getBytes());
            String req2 = """
                    GET / HTTP/1.1\r
                    Accept: text/html\r
                    \r
                    """;
            c.getOutputStream().write(req2.getBytes());
        }
        webServer.stop();
        thread.join();
        Assertions.assertTrue(ctxCalled[0]);
        Assertions.assertTrue(handlerCalled[0]);
        Assertions.assertTrue(interceptorCalled[0]);
    }

    static class RefuseWsHandler implements WebsocketHandler<Void, RequestContext> {
        @Override
        public Void newContext(RequestContext o) {
            return null;
        }

        @Override
        public HandshakeResult onServiceHandshake(String[] availableService, Void ctx) {
            return HandshakeResult.refuse(":(");
        }

        @Override
        public void onHandshakeComplete(WebsocketWriter websocketWriter, Void ctx) { }

        @Override
        public void onMessage(WebsocketMessage msg, Void ctx) { }

        @Override
        public void onClose(Void ctx) { }
    }

    @Test
    public void testWebserverHandlerErrors() {
        var webServer = WebServer.builder().build();
        webServer.registerInterceptor((a, c, n) -> n.handle(a, c));
        webServer.registerHttpHandler("/handler", (r, c) -> new HttpResponse<>(new ByteResponseBody("resp")));
        webServer.registerHttpContext("/handler", (r, c) -> new HttpResponse<>(new ByteResponseBody("<html><body><h1>Ciao</h1></body></html>", "text/html")));
        RefuseWsHandler handler = new RefuseWsHandler();
        webServer.registerWebSocketHandler("/handler", handler);
        webServer.registerWebSocketContext("/handler", handler);

        Assertions.assertThrows(CustomException.class, () ->
                webServer.registerHttpHandler("/handler", (r, c) -> new HttpResponse<>(new ByteResponseBody("<html><body><h1>AltroBody</h1></body></html>", "text/html")))
        );
        Assertions.assertThrows(CustomException.class, () ->
                webServer.registerHttpContext("/handler", (r, c) -> new HttpResponse<>(new ByteResponseBody("<html><body><h1>AltroBody</h1></body></html>", "text/html")))
        );
        Assertions.assertThrows(CustomException.class, () ->
                webServer.registerWebSocketContext("/handler", handler)
        );
        Assertions.assertThrows(CustomException.class, () ->
                webServer.registerWebSocketHandler("/handler", handler)
        );
    }

    @Test
    public void testWebsocketText() throws IOException, InterruptedException {
        var webServer = WebServer.builder()
                .address("localhost")
                .backlog(10)
                .requestContextFactory(RequestContext::new)
                .build();
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

        webServer.registerWebSocketContext("/", new WebsocketHandler<>() {
            @Override
            public Object newContext(RequestContext c) {
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
            public void onHandshakeComplete(WebsocketWriter websocketWriter, Object ctx) {
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

        try(Socket c = new Socket("localhost", webServer.getPort())) {
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

    @Test
    public void testWebSocketPingPong() throws IOException, InterruptedException {
        var webServer = WebServer.builder().build();
        webServer.registerWebSocketContext("/", new WebsocketHandler<>() {
            @Override
            public Object newContext(RequestContext c) {
                return c;
            }

            @Override
            public HandshakeResult onServiceHandshake(String[] availableService, Object o) {
                return HandshakeResult.accept("default");
            }

            @Override
            public void onHandshakeComplete(WebsocketWriter websocketWriter, Object ctx) {}

            @Override
            public void onMessage(WebsocketMessage msg, Object o) {
            }

            @Override
            public void onClose(Object o) {}
        });

        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> webServer.start(semaphore::release));
        thread.start();
        semaphore.acquire();

        try (Socket c = new Socket("localhost", webServer.getPort())) {
            HttpInputStream is = new HttpInputStream(c.getInputStream());
            HttpOutputStream os = new HttpOutputStream(c.getOutputStream());
            os.write("GET /index.html HTTP/1.1\r\n");
            os.writeHeader("Connection", "upgrade");
            os.writeHeader("Upgrade", "websocket");
            os.endHeaders();
            os.flush();

            while (!is.readLine().isEmpty()) {}

            WebSocket webSocket = new WebSocket(c, is, os);

            webSocket.maskAndSendDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_PING, "Ping Test".getBytes(StandardCharsets.UTF_8));

            try (WebSocket.WSDataFrame pongFrame = webSocket.getDataFrame()) {
                assertEquals(WebSocket.WSDataFrame.OP_PONG, pongFrame.opcode);
                assertEquals("Ping Test", new String(pongFrame.body.readAllBytes(), StandardCharsets.UTF_8));
            }

            webSocket.maskAndSendDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_PONG, "Unsolicited pong".getBytes(StandardCharsets.UTF_8));
            webSocket.maskAndSendDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_CLOSE, new byte[0]);
            try (WebSocket.WSDataFrame pongFrame = webSocket.getDataFrame()) {
                assertEquals(WebSocket.WSDataFrame.OP_CLOSE, pongFrame.opcode);
            }

        }

        webServer.stop();
        thread.join();
    }
    @Test
    public void testWebSocketBinary() throws IOException, InterruptedException {
        var webServer = WebServer.builder().build();
        boolean[] binaryMessageReceived = new boolean[1];
        boolean[] errors = new boolean[1];
        byte[][] receivedData = new byte[1][];

        byte[] binaryMessage = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] returnMsg = new byte[] {0x06, 0x07, 0x08, 0x09, 0x0a};


        webServer.registerWebSocketContext("/", new WebsocketHandler<WebsocketWriter[], RequestContext>() {
            @Override
            public WebsocketWriter[] newContext(RequestContext r) {
                return new WebsocketWriter[1];
            }

            @Override
            public HandshakeResult onServiceHandshake(String[] availableService, WebsocketWriter[] o) {
                return HandshakeResult.accept("default");
            }

            @Override
            public void onHandshakeComplete(WebsocketWriter websocketWriter, WebsocketWriter[] ctx) {
                ctx[0] = websocketWriter;
            }

            @Override
            public void onMessage(WebsocketMessage msg, WebsocketWriter[] o) {
                if (msg.type == WebsocketMessage.MsgType.BINARY) {
                    binaryMessageReceived[0] = true;
                    receivedData[0] = msg.data[0];
                    try {
                        o[0].sendBytes(returnMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errors[0] = true;
                    }
                }
            }

            @Override
            public void onClose(WebsocketWriter[] o) {}
        });

        Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread(() -> webServer.start(semaphore::release));
        thread.start();
        semaphore.acquire();

        try (Socket c = new Socket("localhost", webServer.getPort())) {
            HttpInputStream is = new HttpInputStream(c.getInputStream());
            HttpOutputStream os = new HttpOutputStream(c.getOutputStream());
            os.write("GET /index.html HTTP/1.1\r\n");
            os.writeHeader("Connection", "upgrade");
            os.writeHeader("Upgrade", "websocket");
            os.endHeaders();
            os.flush();

            while (!is.readLine().isEmpty()) {}

            WebSocket webSocket = new WebSocket(c, is, os);


            webSocket.maskAndSendDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_BIN, Arrays.copyOf(binaryMessage, binaryMessage.length));
            try (WebSocket.WSDataFrame responseFrame = webSocket.getDataFrame()) {
                assertEquals(WebSocket.WSDataFrame.OP_BIN, responseFrame.opcode);
                assertArrayEquals(returnMsg, responseFrame.body.readAllBytes());
            }

        }

        webServer.stop();
        thread.join();

        assertTrue(binaryMessageReceived[0]);
        assertFalse(errors[0]);
        assertArrayEquals(binaryMessage, receivedData[0]);
    }

    @Test
    public void doubleStartAndStopTest() throws InterruptedException, IOException {
        var webServer = WebServer.builder().serverSocketFactory(() -> new ServerSocket(10010)).build();
        Assertions.assertThrows(CustomException.class, webServer::getPort);
        Semaphore startedSemaphore = new Semaphore(0);
        Assertions.assertThrows(CustomException.class, webServer::stop);
        Thread thread = new Thread(() -> webServer.start(startedSemaphore::release));
        thread.start();
        startedSemaphore.acquire();
        Assertions.assertThrows(CustomException.class, webServer::start);
        webServer.stop();
    }

}
