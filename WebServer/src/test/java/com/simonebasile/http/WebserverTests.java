package com.simonebasile.http;

import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.unpub.CustomException;
import com.simonebasile.http.unpub.HttpInputStream;
import com.simonebasile.http.unpub.HttpOutputStream;
import com.simonebasile.http.unpub.WebSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

public class WebserverTests {

    @Test
    public void testWebserverHttp() throws IOException, InterruptedException {
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        boolean[] ctxCalled = new boolean[1];
        boolean[] handlerCalled = new boolean[1];
        boolean[] interceptorCalled = new boolean[1];
        webServer.registerInterceptor((a, n) -> {
            interceptorCalled[0] = true;
            a.getHeaders().add("Intercepted", "true");
            return n.handle(a);
        });
        webServer.registerHttpHandler("/", (r) -> {
            handlerCalled[0] = true;
            Assertions.assertEquals("true", r.getHeaders().getFirst("Intercepted"));
            return new HttpResponse<>(r.getVersion(), 200, new ByteResponseBody("resp"));
        });
        webServer.registerHttpContext("/", (r) -> {
            ctxCalled[0] = true;
            Assertions.assertEquals("true", r.getHeaders().getFirst("Intercepted"));
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

    static class RefuseWsHandler implements WebsocketHandler<Void> {
        @Override
        public Void newContext() {
            return null;
        }

        @Override
        public HandshakeResult onServiceHandshake(String[] availableService, Void ctx) {
            return HandshakeResult.refuse(":(");
        }

        @Override
        public void onHandshakeComplete(WebsocketWriterImpl websocketWriter, Void ctx) { }

        @Override
        public void onMessage(WebsocketMessage msg, Void ctx) { }

        @Override
        public void onClose(Void ctx) { }
    }

    @Test
    public void testWebserverHandlerErrors() {
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        webServer.registerInterceptor((a, n) -> n.handle(a));
        webServer.registerHttpHandler("/handler", (r) -> new HttpResponse<>(r.getVersion(), new ByteResponseBody("resp")));
        webServer.registerHttpContext("/handler", (r) -> new HttpResponse<>(r.getVersion(), new ByteResponseBody("<html><body><h1>Ciao</h1></body></html>", "text/html")));
        RefuseWsHandler handler = new RefuseWsHandler();
        webServer.registerWebSocketHandler("/handler", handler);
        webServer.registerWebSocketContext("/handler", handler);
        try {
            webServer.registerHttpHandler("/handler", (r) -> new HttpResponse<>(r.getVersion(), new ByteResponseBody("<html><body><h1>AltroBody</h1></body></html>", "text/html")));
            Assertions.fail("Should Throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }
        try {
            webServer.registerHttpContext("/handler", (r) -> new HttpResponse<>(r.getVersion(), new ByteResponseBody("<html><body><h1>AltroBody</h1></body></html>", "text/html")));
            Assertions.fail("Should Throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }
        try {
            webServer.registerWebSocketContext("/handler", handler);
            Assertions.fail("Should Throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }
        try {
            webServer.registerWebSocketHandler("/handler", handler);
            Assertions.fail("Should Throw");
        } catch (Exception e) {
            Assertions.assertInstanceOf(CustomException.class, e);
        }
    }

    @Test
    public void testWebsocketText() throws IOException, InterruptedException {
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

        webServer.registerWebSocketContext("/", new WebsocketHandler<>() {
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

    @Test
    public void testWebSocketPingPong() throws IOException, InterruptedException {
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        webServer.registerWebSocketContext("/", new WebsocketHandler<>() {
            @Override
            public Object newContext() {
                return new Object();
            }

            @Override
            public HandshakeResult onServiceHandshake(String[] availableService, Object o) {
                return HandshakeResult.accept("default");
            }

            @Override
            public void onHandshakeComplete(WebsocketWriterImpl websocketWriter, Object ctx) {}

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

        try (Socket c = new Socket("localhost", port)) {
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
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        boolean[] binaryMessageReceived = new boolean[1];
        boolean[] errors = new boolean[1];
        byte[][] receivedData = new byte[1][];

        byte[] binaryMessage = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] returnMsg = new byte[] {0x06, 0x07, 0x08, 0x09, 0x0a};


        webServer.registerWebSocketContext("/", new WebsocketHandler<WebsocketWriter[]>() {
            @Override
            public WebsocketWriter[] newContext() {
                return new WebsocketWriter[1];
            }

            @Override
            public HandshakeResult onServiceHandshake(String[] availableService, WebsocketWriter[] o) {
                return HandshakeResult.accept("default");
            }

            @Override
            public void onHandshakeComplete(WebsocketWriterImpl websocketWriter, WebsocketWriter[] ctx) {
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

        try (Socket c = new Socket("localhost", port)) {
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
        int port = (int)(Math.random() * 10000) + 2000;
        WebServer webServer = new WebServer(port);
        Semaphore startedSemaphore = new Semaphore(0);
        try {
            webServer.stop();
            Assertions.fail("Should throw");
        }  catch (Exception e) {
            assertInstanceOf(CustomException.class, e);
        }
        Thread thread = new Thread(() -> webServer.start(startedSemaphore::release));
        thread.start();
        startedSemaphore.acquire();
        try {
            webServer.start();
            Assertions.fail("Should throw");
        } catch (Exception e) {
            assertInstanceOf(CustomException.class, e);
        }
        webServer.stop();
    }

}
