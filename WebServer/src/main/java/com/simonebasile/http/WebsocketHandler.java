package com.simonebasile.http;

/**
 * This interface is used to handle websocket connections.
 * It can be overridden by application developers to configure how websocket connections are handled by the web server.
 *
 * @param <WebsocketContext> the type of the websocket context
 * @param <HttpRequestContext> the type of the HTTP request context
 */
public interface WebsocketHandler<WebsocketContext, HttpRequestContext> {

    enum HandshakeResultType{
        Accept,
        Refuse,
    }

    /**
     * This class represents the result of a websocket handshake.
     * It can be used to accept or refuse the handshake.
     * resultType is the type of the result (Accept or Refuse).
     * protocol is the protocol that the server will use to communicate with the client. It is only used if the result is Accept.
     * refuseMessage is the message that the server will send to the client if the handshake is refused. It is only used if the result is Refuse.
     */
    class HandshakeResult {
        public final HandshakeResultType resultType;
        public final String protocol;
        public final String refuseMessage;

        private HandshakeResult(HandshakeResultType resultType, String protocol, String refuseMessage) {
            this.resultType = resultType;
            this.protocol = protocol;
            this.refuseMessage = refuseMessage;
        }

        /**
         * This method is used to create a new HandshakeResult object with the Accept type.
         *
         * @param protocol the protocol that the server will use to communicate with the client
         * @return the new HandshakeResult object
         */
        public static HandshakeResult accept(String protocol) {
            return new HandshakeResult(HandshakeResultType.Accept, protocol, null);
        }

        /**
         * This method is used to create a new HandshakeResult object with the Refuse type.
         *
         * @param message the message that the server will send to the client
         * @return the new HandshakeResult object
         */
        public static HandshakeResult refuse(String message) {
            return new HandshakeResult(HandshakeResultType.Refuse, null, message);
        }
    }


    /**
     * This method is called to create a new websocket context.
     * The websocket context is used to store information about the websocket connection.
     *
     * @param ctx the context of the HTTP request that initiated the websocket connection
     * @return the new websocket context
     */
    WebsocketContext newContext(HttpRequestContext ctx);

    /**
     * This method is called when a client sends a websocket handshake request.
     * implementors should return a HandshakeResult object to accept or refuse the handshake.
     *
     * @param availableService the list of services that the server can provide
     * @param context the context of the websocket connection
     * @return the result of the handshake
     */
    HandshakeResult onServiceHandshake(String[] availableService, WebsocketContext context);

    /**
     * This method is called when the handshake is complete.
     *
     * @param websocketWriter the writer that can be used to send messages to the client
     * @param context the context of the websocket connection
     */
    void onHandshakeComplete(WebsocketWriterImpl websocketWriter, WebsocketContext context);

    /**
     * This method is called when a client sends a message to the server.
     *
     * @param msg the message sent by the client
     * @param context the context of the websocket connection
     */
    void onMessage(WebsocketMessage msg, WebsocketContext context);

    /**
     * This method is called when the client closes the connection.
     *
     * @param context the context of the websocket connection
     */
    void onClose(WebsocketContext context);
}
