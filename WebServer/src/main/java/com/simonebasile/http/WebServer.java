package com.simonebasile.http;

import com.simonebasile.http.unpub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.simonebasile.http.unpub.WebSocket.WSDataFrame.*;

public class WebServer<Context extends RequestContext> implements HttpHandlerContext<InputStream, Context>{
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private final ServerSocketFactory socketFactory;
    private final RequestContextFactory<Context> requestContextFactory;

    private ServerSocket serverSocket;
    private final Lock socketLock;

    private final HttpRoutingContext<InputStream, Context> routingContext;

    private final HandlerRegistry<WebsocketHandler<?, ? super Context>> websocketHandlers;

    private final List<HttpInterceptor<InputStream, Context>> interceptors;

    private WebServer(ServerSocketFactory socketFactory, RequestContextFactory<Context> requestContextFactory) {
        this.socketFactory = socketFactory;
        this.requestContextFactory = requestContextFactory;
        this.websocketHandlers = new HandlerRegistry<>();
        this.routingContext = new HttpRoutingContext<>();
        this.interceptors = new ArrayList<>();
        this.socketLock = new ReentrantLock();
    }

    public static WebServerBuilder<RequestContext> builder() {
        return new WebServerBuilder<>(RequestContext::new);
    }

    public static class WebServerBuilder<Context extends RequestContext> {
        private Integer port;
        private InetAddress address;
        private Integer backlog;
        private ServerSocketFactory serverSocketFactory;
        private RequestContextFactory<?> requestContextFactory;

        private WebServerBuilder(RequestContextFactory<Context> requestContextFactory) {
            this.requestContextFactory = requestContextFactory;
        }

        public WebServerBuilder<Context> port(Integer port) {
            this.port = port;
            return this;
        }

        public WebServerBuilder<Context> address(InetAddress address) {
            this.address = address;
            return this;
        }

        public WebServerBuilder<Context> address(String address) throws UnknownHostException {
            this.address = InetAddress.getByName(address);
            return this;
        }

        public WebServerBuilder<Context> backlog(Integer backlog) {
            this.backlog = backlog;
            return this;
        }

        public WebServerBuilder<Context> serverSocketFactory(ServerSocketFactory serverSocketFactory) {
            this.serverSocketFactory = serverSocketFactory;
            return this;
        }

        public <NewContext extends RequestContext> WebServerBuilder<NewContext> requestContextFactory(RequestContextFactory<NewContext> requestContextFactory) {
            this.requestContextFactory = requestContextFactory;
            return (WebServerBuilder<NewContext>) this;
        }

        public WebServer<Context> build() {
            var serverSocketFactory = this.serverSocketFactory;
            if(serverSocketFactory == null) {
                var port = this.port == null ? 0 : this.port;
                var backlog = this.backlog == null ? 50 : this.backlog;
                var address = this.address;
                serverSocketFactory = () -> new ServerSocket(port, backlog, address);
            }
            return new WebServer<>(
                    serverSocketFactory,
                    (RequestContextFactory<Context>) requestContextFactory
            );

        }
    }

    public int getPort() {
        if(serverSocket == null) {
            throw new CustomException("Server not started");
        } else {
            try {
                return serverSocket.getLocalPort();
            } catch (NullPointerException e) {
                throw new CustomException("Server not started");
            }
        }
    }

    @Override
    public void registerHttpContext(String path, HttpRequestHandler<InputStream, ? super Context> handler){
        routingContext.registerHttpContext(path, handler);
    }

    @Override
    public void registerHttpHandler(String path, HttpRequestHandler<InputStream, ? super Context> handler){
        routingContext.registerHttpHandler(path, handler);
    }

    @Override
    public void registerInterceptor(HttpInterceptor<InputStream, Context> preprocessor) {
        this.interceptors.add(preprocessor);
    }

    public void registerWebSocketContext(String path, WebsocketHandler<?, ? super Context> handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertCtx(path, handler)) {
            throw new CustomException("A websocket context for path [" + path + "] already exists");
        }
    }

    public void registerWebSocketHandler(String path, WebsocketHandler<?, ? super Context> handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertExact(path, handler)) {
            throw new CustomException("A websocket handler for path [" + path + "] already exists");
        }
    }


    public void stop() throws IOException {
        socketLock.lock();
        try {
            if(serverSocket == null) {
                throw new CustomException("Server is not running");
            }
            serverSocket.close();
        } finally {
            socketLock.unlock();
        }

    }

    public void start() {
        start(null);
    }
    public void start(Runnable onstart) {
        final ExecutorService executor = Executors.newCachedThreadPool();
        socketLock.lock();
        if(serverSocket != null) {
            socketLock.unlock();
            throw new CustomException("Server is already running");
        }
        try {
            try {
                serverSocket = socketFactory.createSocket();
            } catch (Exception e) {
                log.error("An error occurred while starting the server: {}", e.getMessage(), e);
                return;
            } finally {
                socketLock.unlock();
            }
            if(onstart != null) {
                onstart.run();
            }
            log.info("Server started {}", serverSocket);
            while(!serverSocket.isClosed()) {
                HttpProtocolHandler handler;
                Socket c;
                try {
                    c = serverSocket.accept();
                    //TODO add timeout c.setSoTimeout();
                } catch (IOException e) {
                    log.error("An exception occurred while accepting the socket", e);
                    continue;
                }
                try {
                    handler = new HttpProtocolHandler(c);
                } catch (IOException e) {
                    log.error("An exception occurred while creating the handler", e);
                    try {
                        c.close();
                    } catch (IOException closeException) {
                        log.error("An exception occurred while closing the socket", closeException);
                    }
                    continue;
                }
                try {
                    executor.submit(handler);
                } catch (RejectedExecutionException e) {
                    log.error("Error submitting handler for execution", e);
                    handler.close();
                }
            }
        } finally {
            socketLock.lock();
            try {
                if(serverSocket != null) {
                    try {
                        serverSocket.close();
                        serverSocket = null;
                    } catch (IOException closeEx) {
                        log.error("Error closing server socket {}", serverSocket, closeEx);
                    }
                }
            } finally {
                socketLock.unlock();
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                log.error("Interrupted before finishing all tasks");
            }
        }
    }

    class HttpProtocolHandler implements Runnable, AutoCloseable {
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
            this.inputStream = new HttpInputStream(client.getInputStream());
            this.outputStream = new HttpOutputStream(client.getOutputStream());
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
                while (true) {
                    HttpRequest<InputStream> req = HttpRequest.parse(inputStream, FixedLengthInputStream::new);
                    Context context = requestContextFactory.createContext();
                    log.debug("Incoming http request [{}] on socket [{}]", req, client);
                    HttpResponse<?> res = new InterceptorChainImpl<>(interceptors, (r, c) -> {
                        if (r.isWebSocketConnection()) {
                            try {
                                handleWebsocket(discardBody(r), c);
                                throw new HandledByWebsocket();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return routingContext.handle(r, c);
                        }
                    }).handle(req, context);

                    //consuming remaining body
                    req.body.close();

                    res.write(outputStream);
                    //TODO handle connection and keep-alive header, handle timeouts, handle max amt of requests
                }
            } catch (ConnectionClosedBeforeRequestStartException ignored) {
                log.debug("Client closed connection");
            } catch (HandledByWebsocket e) {
                log.debug("Closed websocket");
            } catch (Throwable t) {
                log.error("An exception occurred while handling http protocol. Closing socket [{}]", client, t);
            } finally {
                closeClient();
            }
        }

        private HttpRequest<Void> discardBody(HttpRequest<? extends InputStream> req) throws IOException {
            req.body.close();
            return new HttpRequest<>(req.method, req.resource, req.version, req.headers, null);
        }

        private void handleWebsocket(HttpRequest<Void> req, Context context) throws IOException {
            var match = websocketHandlers.getHandler(req.getResource());
            WebsocketHandler<?, ? super Context> wsHandler = match.handler();
            context.setContextMatch(match.match());
            _handleWebsocket(req, context, wsHandler);
        }

        private <T> void _handleWebsocket(HttpRequest<Void> req, Context context, WebsocketHandler<T, ? super Context> wsHandler) throws IOException {
            final T ctx = wsHandler.newContext(context);
            final HttpHeaders headers = req.getHeaders();
            final String protocolsHeader = headers.getFirst("Sec-WebSocket-Protocol");
            final String[] protocols = protocolsHeader == null ? new String[0] : protocolsHeader.split(",");
            final WebsocketHandler.HandshakeResult handshakeResult = wsHandler.onServiceHandshake(protocols, ctx);
            if(handshakeResult.resultType == WebsocketHandler.HandshakeResultType.Accept) {
                String wsSec = req.getHeaders().getFirst("Sec-WebSocket-Key");
                String wsAccept = Base64.getEncoder().encodeToString(SHA1.digest((wsSec + MAGIC).getBytes(StandardCharsets.UTF_8)));
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add("Upgrade",  "websocket");
                httpHeaders.add("Connection", "Upgrade");
                httpHeaders.add("Sec-WebSocket-Accept", wsAccept);
                new HttpResponse<>(req.getVersion(), 101, httpHeaders, null).write(outputStream);
                boolean hsComplete = false;
                try {
                    final WebSocket webSocket = new WebSocket(client, inputStream, outputStream);
                    wsHandler.onHandshakeComplete(new WebsocketWriterImpl(webSocket), ctx);
                    hsComplete = true;
                    boolean close = false;
                    while(!close) { //Message
                        //TODO streaming
                        final ArrayList<byte[]> bytes = new ArrayList<>();
                        boolean last = false;
                        int opcode = -1;
                        while(!last) {
                            try (final WebSocket.WSDataFrame dataFrame = webSocket.getDataFrame()){
                                if(!dataFrame.masked) {
                                    throw new CustomException("Dataframe is not masked");
                                }
                                switch(dataFrame.opcode) {
                                    case OP_CLOSE:
                                        log.debug("Received close");
                                        webSocket.sendUnmaskedDataframe(FIN, OP_CLOSE, new byte[0]);
                                        close = true;
                                        last = true;
                                        opcode = -1;
                                        break;
                                    case OP_PING:
                                        webSocket.sendUnmaskedDataframe(FIN, OP_PONG, dataFrame.body.readAllBytes());
                                        log.debug("Received ping");
                                        opcode = -1;
                                        break;
                                    case OP_PONG:
                                        log.debug("Received pong");
                                        opcode = -1;
                                        break;
                                    case OP_TEXT:
                                    case OP_BIN:
                                        final int flags = dataFrame.flags;
                                        if(opcode == -1) {
                                            opcode = dataFrame.opcode;
                                        } else if(opcode != dataFrame.opcode){
                                            log.error("Unexpected opcode change [last: {}, current: {}]", opcode, dataFrame.opcode);
                                            throw new CustomException("Opcode changed in packet chain");
                                        }
                                        last = (flags & WebSocket.WSDataFrame.FIN) == WebSocket.WSDataFrame.FIN;
                                        bytes.add(dataFrame.body.readAllBytes());
                                }
                            }
                        }
                        if(!bytes.isEmpty()) {
                            WebsocketMessage.MsgType type;
                            if (opcode == OP_TEXT) {
                                type = WebsocketMessage.MsgType.TEXT;
                            } else if(opcode == WebSocket.WSDataFrame.OP_BIN){
                                type = WebsocketMessage.MsgType.BINARY;
                            } else {
                                continue;
                            }
                            wsHandler.onMessage(new WebsocketMessage(bytes.toArray(new byte[bytes.size()][]), type), ctx);
                        }
                    }
                    wsHandler.onClose(ctx);
                } catch (Exception e) {
                    if(hsComplete) wsHandler.onClose(ctx);
                    //TODO review error handling
                    log.error("Error in websocket", e);
                }
            } else {
                new HttpResponse<>(req.getVersion(), 400, new HttpHeaders(), null).write(outputStream);
            }
        }



        @Override
        public void close() {
            closeClient();
        }
    }

}
