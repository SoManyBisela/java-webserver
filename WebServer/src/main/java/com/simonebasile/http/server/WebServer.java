package com.simonebasile.http.server;

import com.simonebasile.http.handlers.HttpInterceptor;
import com.simonebasile.http.handlers.HttpRequestHandler;
import com.simonebasile.http.handlers.WebsocketHandler;
import com.simonebasile.http.handlers.WebsocketMessage;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.routing.HandlerRegistry;
import com.simonebasile.http.routing.HttpRoutingContext;
import com.simonebasile.http.routing.HttpRoutingContextImpl;
import com.simonebasile.http.unexported.*;
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

import static com.simonebasile.http.unexported.WebSocket.WSDataFrame.*;

/**
 * A web server that can handle both http and websocket requests.
 * The server can be configured to listen on a specific port and address. Alternatively, a custom {@link ServerSocketFactory} can be used.
 * It can be configured to use a custom {@link RequestContextFactory} to create a custom context to be passed to the handlers.
 *
 * Handler can be registered for both http and websocket requests.
 * Http handlers can be registered for specific paths or for all paths that match a prefix.
 * Interceptors can be registered to preprocess requests before the handler.
 *
 * Handler will be called according to the rules specified in the {@link HttpRoutingContextImpl} class.
 *
 * The server can be started and stopped using the {@link #start()} and {@link #stop()} methods.
 *
 * The server runs on a main thread that will accept incoming connections.
 * For each incoming connection, a thread from a cached thread pool will be used to handle the connection.
 * In case of a websocket upgrade request, the thread will handle the websocket connection until it is closed.
 *
 * @param <Context> The type of the context object that will be passed to the handlers
 */
public class WebServer<Context extends RequestContext> implements HttpRoutingContext<InputStream, Context> {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private final ServerSocketFactory socketFactory;
    private final RequestContextFactory<Context> requestContextFactory;

    private ServerSocket serverSocket;
    private final Lock socketLock;

    private final HttpRoutingContextImpl<InputStream, Context> routingContext;

    private final HandlerRegistry<WebsocketHandler<?, ? super Context>> websocketHandlers;

    private final List<HttpInterceptor<InputStream, Context>> interceptors;

    private WebServer(ServerSocketFactory socketFactory, RequestContextFactory<Context> requestContextFactory) {
        this.socketFactory = socketFactory;
        this.requestContextFactory = requestContextFactory;
        this.websocketHandlers = new HandlerRegistry<>();
        this.routingContext = new HttpRoutingContextImpl<>();
        this.interceptors = new ArrayList<>();
        this.socketLock = new ReentrantLock();
    }

    /**
     * Creates a new builder for the server.
     * @return A new builder
     */
    public static WebServerBuilder<RequestContext> builder() {
        return new WebServerBuilder<>(new DefaultRequestContextFactory());
    }

    /**
     * The builder for the server. It allows to configure the server before instantiating it.
     */
    public static class WebServerBuilder<Context extends RequestContext> {
        private Integer port;
        private InetAddress address;
        private Integer backlog;
        private ServerSocketFactory serverSocketFactory;
        private RequestContextFactory<?> requestContextFactory;

        /**
         * Creates a new builder with the given request context factory.
         * @param requestContextFactory The factory to create the context object
         */
        private WebServerBuilder(RequestContextFactory<Context> requestContextFactory) {
            this.requestContextFactory = requestContextFactory;
        }

        /**
         * configures the port on which the server will listen.
         * setting the port to 0 will let the system choose a random port among the available ones.
         * @param port The port number
         * @return This builder
         */
        public WebServerBuilder<Context> port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Configures the address on which the server will listen.
         * @param address The address
         * @return This builder
         */
        public WebServerBuilder<Context> address(InetAddress address) {
            this.address = address;
            return this;
        }

        /**
         * Configures the address on which the server will listen.
         * @param address The address
         * @return This builder
         * @throws UnknownHostException If the address is not valid
         */
        public WebServerBuilder<Context> address(String address) throws UnknownHostException {
            this.address = InetAddress.getByName(address);
            return this;
        }

        /**
         * Configures the backlog for the server socket.
         * @param backlog The backlog
         * @return This builder
         */
        public WebServerBuilder<Context> backlog(Integer backlog) {
            this.backlog = backlog;
            return this;
        }

        /**
         * Configures a custom server socket factory.
         * Configuring a custom server socket factory will override the port, address and backlog settings.
         * @param serverSocketFactory The factory
         * @return This builder
         */
        public WebServerBuilder<Context> serverSocketFactory(ServerSocketFactory serverSocketFactory) {
            this.serverSocketFactory = serverSocketFactory;
            return this;
        }

        /**
         * Configures a custom request context factory.
         * @param requestContextFactory The factory
         * @return This builder
         */
        public <NewContext extends RequestContext> WebServerBuilder<NewContext> requestContextFactory(RequestContextFactory<NewContext> requestContextFactory) {
            this.requestContextFactory = requestContextFactory;

            //this cast is necessary to tell the compiler that the webserver that will be built will have the new context type
            //allowing appropriate handlers to be registered
            //The cast is safe since the context factory replaces the previous one
            return  (WebServerBuilder<NewContext>) this;
        }

        /**
         * Builds the server with the configured settings.
         * @return The server
         */
        public WebServer<Context> build() {
            var serverSocketFactory = this.serverSocketFactory;
            if(serverSocketFactory == null) {
                var port = this.port == null ? 80 : this.port;
                var backlog = this.backlog == null ? 50 : this.backlog;
                var address = this.address;
                serverSocketFactory = new DefaultServerSocketFactory(port, backlog, address);
            }
            return new WebServer<>(
                    serverSocketFactory,
                    (RequestContextFactory<Context>) requestContextFactory
            );

        }

    }

    /**
     * Gets the port on which the server is listening.
     * @return The port
     * @throws CustomException If the server is not running
     */
    public int getPort() {
        if(serverSocket == null) {
            throw new CustomException("Server not started");
        } else {
            try {
                return serverSocket.getLocalPort();
            } catch (NullPointerException e) {
                //may occur if the server is closed while calling this method
                throw new CustomException("Server not started");
            }
        }
    }

    /**
     * Registers a new http context handler for the given path.
     * @param path The path to register the context for
     * @param handler The handler to register
     */
    @Override
    public void registerHttpContext(String path, HttpRequestHandler<InputStream, ? super Context> handler){
        routingContext.registerHttpContext(path, handler);
    }

    /**
     * Registers a new http handler for the given path.
     * @param path The path to register the handler for
     * @param handler The handler to register
     */
    @Override
    public void registerHttpHandler(String path, HttpRequestHandler<InputStream, ? super Context> handler){
        routingContext.registerHttpHandler(path, handler);
    }

    /**
     * Registers a new interceptor for the server.
     * Interceptors will be called in order of registration before the handler is called.
     * @param interceptor The interceptor to register
     */
    @Override
    public void registerInterceptor(HttpInterceptor<InputStream, Context> interceptor) {
        this.interceptors.add(interceptor);
    }

    /**
     * Registers a new websocket context handler for the given path.
     * @param path The path to register the context for
     * @param handler The handler to register
     */
    public void registerWebSocketContext(String path, WebsocketHandler<?, ? super Context> handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertCtx(path, handler)) {
            throw new CustomException("A websocket context for path [" + path + "] already exists");
        }
    }

    /**
     * Registers a new websocket handler for the given path.
     * @param path The path to register the handler for
     * @param handler The handler to register
     */
    public void registerWebSocketHandler(String path, WebsocketHandler<?, ? super Context> handler){
        log.debug("Registered new websocket handler for path [{}]", path);
        if(!websocketHandlers.insertExact(path, handler)) {
            throw new CustomException("A websocket handler for path [" + path + "] already exists");
        }
    }


    /**
     * Stops the server.
     * If the server is not running, an exception will be thrown.
     * @throws IOException If an error occurs while closing the server socket
     * @throws CustomException If the server is not running
     */
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

    /**
     * Starts the server.
     */
    public void start() {
        start(null);
    }
    /**
     * Starts the server.
     *
     * The server will run on the thread calling this method.
     * A socket will be created and the server will start listening for incoming connections.
     * Each incoming connection will be handled by a thread from a cached thread pool.
     * @param onstart A runnable that will be executed after the server is started
     * @throws CustomException If the server is already running
     */
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

    /**
     * A handler for the http protocol.
     * The handler will read the requests perform the routing and call the appropriate handler.
     * If the request is a websocket upgrade request, the handler will handle the websocket connection.
     */
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
        private final HttpInputStream inputStream;
        private final HttpOutputStream outputStream;

        /**
         * Creates a new handler for the given socket.
         * @param client The socket to handle
         * @throws IOException If an error occurs while creating the input and output streams
         */
        public HttpProtocolHandler(Socket client) throws IOException {
            this.client = Objects.requireNonNull(client);
            this.inputStream = new HttpInputStream(client.getInputStream());
            this.outputStream = new HttpOutputStream(client.getOutputStream());
        }

        /**
         * Closes the client socket.
         */
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

        /**
         * Handles the http protocol.
         */
        @Override
        public void run() {
            log.debug("Started http handler for socket {}", client);
            try {
                boolean[] wsHandled = {false};
                while (true) {
                    HttpRequest<InputStream> req = HttpMessageUtils.parseRequest(inputStream);
                    Context context = requestContextFactory.createContext();
                    log.debug("Incoming http request [{}] on socket [{}]", req, client);
                    HttpResponse<? extends HttpResponseBody> res = new InterceptorChainImpl<>(interceptors, (r, c) -> {
                        if (r.isWebSocketConnection()) {
                            try {
                                handleWebsocket(discardBody(r), c);
                                wsHandled[0] = true;
                                return new HttpResponse<>(200, null);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return routingContext.handle(r, c);
                        }
                    }).handle(req, context);
                    if(wsHandled[0]) {
                        break;
                    }
                    //consuming remaining body
                    req.getBody().close();

                    HttpMessageUtils.writeResponse(req.getVersion(), res, outputStream);
                    //TODO handle connection and keep-alive header, handle timeouts, handle max amt of requests
                }
            } catch (ConnectionClosedBeforeRequestStartException ignored) {
                log.debug("Client closed connection");
            } catch (Throwable t) {
                log.error("An exception occurred while handling http protocol. Closing socket [{}]", client, t);
            } finally {
                closeClient();
            }
        }

        /**
         * Discards and consumes the body of the request.
         * @param req The request to discard the body of
         * @return A new request with the body discarded
         * @throws IOException If an error occurs while closing the body
         */
        private HttpRequest<Void> discardBody(HttpRequest<? extends InputStream> req) throws IOException {
            req.getBody().close();
            return new HttpRequest<>(req, null);
        }

        /**
         * Handles a websocket upgrade request.
         * @param req The request to handle
         * @param context The context to pass to the handler
         * @throws IOException If an error occurs while handling the websocket
         */
        private void handleWebsocket(HttpRequest<Void> req, Context context) throws IOException {
            var match = websocketHandlers.getHandler(req.getResource());
            WebsocketHandler<?, ? super Context> wsHandler = match.handler();
            context.setContextMatch(match.match());
            _handleWebsocket(req, context, wsHandler);
        }

        /**
         * Utility method to handle the websocket upgrade request.
         * This method is required to handle the generic type of the websocket handler.
         *
         * This method is responsible for the handshake and the message handling of the websocket.
         */
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
                HttpMessageUtils.writeResponse(req.getVersion(), new HttpResponse<>(101, httpHeaders, null), outputStream);
                boolean hsComplete = false;
                try {
                    final WebSocket webSocket = new WebSocket(client, inputStream, outputStream);
                    wsHandler.onHandshakeComplete(new WebsocketWriterImpl(webSocket), ctx);
                    hsComplete = true;
                    boolean close = false;
                    while(!(close || webSocket.isCloseSent())) { //Message
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
                                        wsHandler.onClose(ctx);
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
                    if(!close) {
                        wsHandler.onClose(ctx);
                    }
                } catch (Exception e) {
                    if(hsComplete) wsHandler.onClose(ctx);
                    //TODO review error handling
                    log.error("Error in websocket", e);
                }
            } else {
                HttpMessageUtils.writeResponse(req.getVersion(), new HttpResponse<>(
                        400,
                        new HttpHeaders(),
                        new ByteResponseBody(handshakeResult.refuseMessage)
                ), outputStream);
            }
        }

        /**
         * Closes the client socket.
         */
        @Override
        public void close() {
            closeClient();
        }
    }

}
