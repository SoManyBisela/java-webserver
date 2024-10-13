package com.simonebasile.http;

import com.simonebasile.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class WebServer implements HttpHandlerContext<InputStream>{
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private final int port;

    private final HttpRoutingContext routingContext;

    private final HandlerRegistry<WebsocketConnectionHandler> websocketHandlers;

    public WebServer(int port) {
        this.port = port;
        this.websocketHandlers = new HandlerRegistry<>();
        this.routingContext = new HttpRoutingContext();
    }

    public void registerHttpContext(String path, HttpRequestHandler<InputStream> handler){
        routingContext.registerHttpContext(path, handler);
    }

    public void registerHttpHandler(String path, HttpRequestHandler<InputStream> handler){
        routingContext.registerHttpHandler(path, handler);
    }

    @Override
    public void registerPreprocessor(HttpRequestPreprocessor<InputStream> preprocessor) {
        //TODO register preprocessors
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void registerWebSocketContext(String path, WebsocketConnectionHandler handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertCtx(path, handler)) {
            throw new CustomException("A websocket context for path [" + path + "] already exists");
        }
    }

    public void registerWebSocketHandler(String path, WebsocketConnectionHandler handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertExact(path, handler)) {
            throw new CustomException("A websocket handler for path [" + path + "] already exists");
        }
    }

    class HttpProtocolHandler implements Runnable, AutoCloseable {
        private static final Logger log = LoggerFactory.getLogger(HttpProtocolHandler.class);
        private static final String MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        private static final MessageDigest SHA1 = initSha1();

        private static MessageDigest initSha1() {
            try {
                return MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private Socket client;
        private HttpInputStream inputStream;
        private HttpOutputStream outputStream;

        public HttpProtocolHandler(Socket client) throws IOException {
            this.client = Objects.requireNonNull(client);
            try {
                this.inputStream = new HttpInputStream(client.getInputStream());
                this.outputStream = new HttpOutputStream(client.getOutputStream());
            } catch (Exception e) {
                closeClient();
                throw e;
            }
        }

        private void closeClient() {
            if(client != null) {
                try {
                    client.close();
                    client = null;
                } catch (IOException e) {
                    log.error("An exception occurred while closing socket {}", client, e);
                }
            }
        }

        @Override
        public void run() {
            log.debug("Started http handler for socket {}", client);
            try {
                while(true) {
                    HttpRequest<InputStream> req = HttpRequest.parse(inputStream, FixedLengthInputStream::new);
                    //TODO preprocess here
                    if(req.isWebSocketConnection()) {
                        handoffConnection(discardBody(req));
                        break;
                    } else {
                        HttpResponse<?> response = routingContext.handle(req);
                        response.write(outputStream);
                    }
                    //TODO handle connection and keep-alive header, handle timeouts, handle max amt of requests
                }
            } catch (Throwable t) {
                log.error("An exception occurred while handling http protocol. Closing socket [{}]", client, t);
            } finally {
                closeClient();
            }
        }

        private void notFound(HttpRequest<?> req) throws IOException {
            outputStream.writeStatus(req.getVersion(), 404, "Not found");
            outputStream.writeHeader("Content-Type", "text/plain; charset=utf-8");
            outputStream.writeBody("Resource not found".getBytes(StandardCharsets.UTF_8));
        }

        private HttpRequest<Void> discardBody(HttpRequest<InputStream> req) throws IOException {
            req.body.close();
            return new HttpRequest<>(req.method, req.resource, req.version, req.headers, null);
        }

        private void handoffConnection(HttpRequest<Void> req) throws IOException {
            log.debug("Incoming websocket connection [{}]", req);
            var wsHandler = websocketHandlers.getHandler(req.getResource());
            if(wsHandler != null) {
                //Complete websocket handshake
                String wsSec = req.getHeaders().getFirst("Sec-WebSocket-Key");
                String wsAccept = Base64.getEncoder().encodeToString(SHA1.digest((wsSec + MAGIC).getBytes(StandardCharsets.UTF_8)));
                outputStream.writeStatus(req.getVersion(), 101, "Switching Protocols");
                outputStream.writeHeader("Upgrade",  "websocket");
                outputStream.writeHeader("Connection", "Upgrade");
                outputStream.writeHeader("Sec-WebSocket-Accept", wsAccept);
                outputStream.end();
                wsHandler.newConnection(new WebSocket(req, client, inputStream, outputStream));
                //setting the connection to null to avoid closing the socket as it is now owned by the wsHandler
                client = null;
            } else {
                notFound(req);
            }
        }

        @Override
        public void close() {
            closeClient();
        }
    }

    public void start() {
        final ExecutorService executor = Executors.newCachedThreadPool();
        ServerSocket s = null;
        try {
            s = new ServerSocket(port);
            log.info("Server started on port {}", port);
            while(true) {
                HttpProtocolHandler handler;
                try {
                    Socket c = s.accept();
                    //TODO add timeout c.setSoTimeout();
                    handler = new HttpProtocolHandler(c);
                } catch (IOException e) {
                    log.error("An exception occurred while accepting the socket", e);
                    continue;
                }
                try {
                    executor.submit(handler);
                } catch (RejectedExecutionException e) {
                    log.error("Error submitting handler for execution", e);
                    handler.close();
                }
            }
        } catch (IOException e) {
            log.error("Error starting http server", e);
            throw new CustomException("Error starting http server", e);
        } finally {
            if(s != null) {
                try {
                    s.close();
                } catch (IOException closeEx) {
                    log.error("Error closing server socket {}", s, closeEx);
                }
            }
        }
    }

}
