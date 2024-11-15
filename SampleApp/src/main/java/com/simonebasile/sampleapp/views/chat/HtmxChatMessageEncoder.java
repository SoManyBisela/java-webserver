package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.controllers.ChatMessageEncoder;
import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.simonebasile.sampleapp.views.html.HtmlElement.div;

public class HtmxChatMessageEncoder implements ChatMessageEncoder {
    private static HtmlElement obswap(String id, HtmlElement el) {
        return obswap(id, "true", el);
    }

    private static HtmlElement obswap(String id, String swap, HtmlElement el) {
        return el.attr( "id", id).hxSwapOob(swap);
    }

    private IHtmlElement fromMessage(ChatProtoMessage msg) {
        return  switch (msg.getType()) {
            case CONNECTED -> obswap("chat-container", div().content(
                    new WantToChatElement()
            ));
            case WAIT_FOR_CHAT -> obswap("chat-container", div().content(
                    div().text("Waiting for connection"),
                    new StopWaitingElement()
            ));
            case ALREADY_CONNECTED -> obswap("chat-section", new AlreadyConnectedSection());
            case CHAT_CONNECTED -> obswap("chat-container", div().content(
                    div().text("Connected with: " + msg.getUsername()),
                    div().attr("id", "messages", "class", "message-container"),
                    div().attr("id", "chat-inputs-container").content(
                            new SendMessageElement(),
                            new EndChatElement()
                    )
            ));
            case CHAT_DISCONNECTED -> obswap("chat-inputs-container", div().content(
                    div().text("Chat disconnected"),
                    new RestartChatElement()
            ));
            case MESSAGE_RECEIVED ->
                    obswap("messages", "beforeend", div().content(
                            div().attr("class", "message-row received").content(
                                    div().attr("class", "message").text(msg.getMessage()))));
            case MESSAGE_SENT ->
                    new ElementGroup(
                            obswap("messages", "beforeend", div().content(
                                    div().attr("class", "message-row sent").content(
                                            div().attr("class", "message").text(msg.getMessage())))),
                            new SendMessageElement().focusOnLoad()
                    );
            case CHAT_AVAILABLE -> obswap("chat-container", div().content(
                    new ElementGroup(
                            div().text("There are users waiting to chat"),
                            new AcceptChatElement()
                    )
            ));
            case NO_CHAT_AVAILABLE -> obswap("chat-container", div().content(
                    div().text("There are no chat requests")
            ));
            default -> throw new IllegalStateException("Unexpected type: " + msg.getType());
        };
    }

    @Override
    public byte[] encode(ChatProtoMessage message) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            fromMessage(message).write(output);
        } catch (IOException e) {
            //writing to a ByteArrayOutputStream can't throw IOException
        }
        return output.toByteArray();
    }
}
