package com.simonebasile.http;

import com.simonebasile.CustomException;
import com.simonebasile.http.response.ByteResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class WebServer implements HttpHandlerContext<InputStream>{
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private final int port;

    private final HttpRoutingContext routingContext;

    private final HandlerRegistry<WebsocketConnectionHandler> websocketHandlers;

    private final List<HttpInterceptor<InputStream>> interceptors;

    public WebServer(int port) {
        this.port = port;
        this.websocketHandlers = new HandlerRegistry<>();
        this.routingContext = new HttpRoutingContext();
        this.interceptors = new ArrayList<>();
    }

    public void registerHttpContext(String path, HttpRequestHandler<InputStream> handler){
        routingContext.registerHttpContext(path, handler);
    }

    public void registerHttpHandler(String path, HttpRequestHandler<InputStream> handler){
        routingContext.registerHttpHandler(path, handler);
    }

    @Override
    public void registerInterceptor(HttpInterceptor<InputStream> preprocessor) {
        this.interceptors.add(preprocessor);
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
        private Handoff handoff;

        public HttpProtocolHandler(Socket client) throws IOException {
            this.client = Objects.requireNonNull(client);
            this.handoff = null;
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
                    HttpResponse<?> res = new InterceptorChainImpl<>(interceptors, r -> {
                        if(req.isWebSocketConnection()) {
                            try {
                                return prepareHandoff(discardBody(req));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return routingContext.handle(req);
                        }
                    }).handle(req);
                    res.write(outputStream);
                    if(this.handoff != null) {
                        completeHandoff();
                        break;
                    }
                    //TODO handle connection and keep-alive header, handle timeouts, handle max amt of requests
                }
            } catch (Throwable t) {
                log.error("An exception occurred while handling http protocol. Closing socket [{}]", client, t);
            } finally {
                closeClient();
            }
        }

        private HttpRequest<Void> discardBody(HttpRequest<InputStream> req) throws IOException {
            req.body.close();
            return new HttpRequest<>(req.method, req.resource, req.version, req.headers, null);
        }

        private HttpResponse<? extends HttpResponse.ResponseBody> prepareHandoff(HttpRequest<Void> req) throws IOException {
            log.debug("Incoming websocket connection [{}]", req);
            WebsocketConnectionHandler wsHandler = websocketHandlers.getHandler(req.getResource());
            //TODO The websocket handler should have a way of reading the request and responding with some additional info
            if(wsHandler != null) {
                this.handoff = new Handoff(wsHandler, req);
                //Complete websocket handshake
                String wsSec = req.getHeaders().getFirst("Sec-WebSocket-Key");
                String wsAccept = Base64.getEncoder().encodeToString(SHA1.digest((wsSec + MAGIC).getBytes(StandardCharsets.UTF_8)));
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Upgrade",  "websocket");
                httpHeaders.add("Connection", "Upgrade");
                httpHeaders.add("Sec-WebSocket-Accept", wsAccept);
                return new HttpResponse<>(req.getVersion(), 101, httpHeaders, null);
            } else {
                return new HttpResponse<>(req.getVersion(), 404,
                        new HttpHeaders(), new ByteResponseBody("Resource not found"));
            }
        }

        private void completeHandoff() {
            handoff.wsHandler.newConnection(new WebSocket(handoff.req, client, inputStream, outputStream));
            //setting the connection to null to avoid closing the socket as it is now owned by the wsHandler
            client = null;
        }


        @Override
        public void close() {
            closeClient();
        }

        private record Handoff(WebsocketConnectionHandler wsHandler, HttpRequest<Void> req) {
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
