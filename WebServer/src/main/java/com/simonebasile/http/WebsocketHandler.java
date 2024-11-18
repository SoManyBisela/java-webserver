package com.simonebasile.http;


public interface WebsocketHandler<WebsocketContext, HttpRequestContext> {

    enum HandshakeResultType{
        Accept,
        Refuse,
    }

    class HandshakeResult {
        public final HandshakeResultType resultType;
        public final String protocol;
        public final String refuseMessage;

        private HandshakeResult(HandshakeResultType resultType, String protocol, String refuseMessage) {
            this.resultType = resultType;
            this.protocol = protocol;
            this.refuseMessage = refuseMessage;
        }

        public static HandshakeResult accept(String protocol) {
            return new HandshakeResult(HandshakeResultType.Accept, protocol, null);
        }

        public static HandshakeResult refuse(String message) {
            return new HandshakeResult(HandshakeResultType.Refuse, null, message);
        }
    }


    WebsocketContext newContext(HttpRequestContext ctx);

    HandshakeResult onServiceHandshake(String[] availableService, WebsocketContext context);

    void onHandshakeComplete(WebsocketWriterImpl websocketWriter, WebsocketContext context);

    void onMessage(WebsocketMessage msg, WebsocketContext context);

    void onClose(WebsocketContext context);
}
