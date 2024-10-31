package com.simonebasile.http;


public interface NewWsHandler<Context> {


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


    Context newContext();

    HandshakeResult serviceHandshake(String[] availableService, Context context);

    void onHandshakeComplete(WebsocketWriter websocketWriter, Context ctx);

    void onMessage(WebsocketMessage msg, Context context);

    void onClose(Context context);
}
