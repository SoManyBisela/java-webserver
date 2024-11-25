package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.dto.ChatMessageEncoder;
import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;
import static com.simonebasile.sampleapp.views.html.HtmlElement.span;

/**
 * Class that encodes a ChatProtoMessage into htmx elements to be rendered in the page
 */
public class HtmxChatMessageEncoder implements ChatMessageEncoder {
    /**
     * Utility method to add the attributes to an element and swap it in the page
     * @param id the id of the element to swap
     * @param el the element to swap with
     * @return the element with the attributes set
     */
    private static HtmlElement obswap(String id, HtmlElement el) {
        return obswap(id, "true", el);
    }

    /**
     * Utility method to add the attributes to an element and swap it in the page
     * @param id the id of the element to swap
     * @param swap the value of the swap attribute
     * @param el the element to swap with
     * @return the element with the attributes set
     */
    private static HtmlElement obswap(String id, String swap, HtmlElement el) {
        return el.attr( "id", id).hxSwapOob(swap);
    }

    /**
     * Encodes a ChatProtoMessage into htmx elements
     * @param msg the message to encode
     * @return the htmx elements
     */
    private IHtmlElement fromMessage(ChatProtoMessage msg) {
        return  switch (msg.getType()) {
            case CONNECTED -> obswap("chat-container", div().attr("class", "stack-vertical").content(
                    div().text("Click to request a chat with an employee"),
                    new WantToChatElement()
            ));
            case WAIT_FOR_CHAT -> obswap("chat-container", div().attr("class", "stack-vertical").content(
                    div().text("Waiting for available employee"),
                    new StopWaitingElement()
            ));
            case ALREADY_CONNECTED -> obswap("chat-section", new AlreadyConnectedSection());
            case CHAT_CONNECTED -> new ElementGroup(
                    obswap("chat-container", div().content(
                            div().attr("class", "message-area").content(div().attr("id", "messages", "class", "message-container")),
                            div().attr("id", "chat-inputs-container").content(
                                    new SendMessageElement()
                            ))),
                    new ChatTitle(msg.getUsername()).hxSwapOob("true"),
                    new EndChatElement().hxSwapOob("true")
            );
            case CHAT_DISCONNECTED -> new ElementGroup(
                    obswap("chat-inputs-container", div().content(
                            div().text("Chat disconnected"),
                            new RestartChatElement()
                    )),
                    new ChatTitle("Chat").hxSwapOob("true"),
                    new EndChatElement().hidden().hxSwapOob("true")
            );
            case MESSAGE_RECEIVED ->
                    obswap("messages", "beforeend", div().content(
                            scrollIntoView(div().attr("class", "message-row received").content(
                                    div().attr("class", "message").text(msg.getMessage())))));
            case MESSAGE_SENT ->
                    new ElementGroup(
                            obswap("messages", "beforeend", div().content(
                                    scrollIntoView(div().attr("class", "message-row sent").content(
                                            div().attr("class", "message").text(msg.getMessage()))))),
                            new SendMessageElement().focusOnLoad()
                    );
            case CHAT_AVAILABLE -> obswap("chat-container", div().attr("class", "stack-vertical").content(
                            div().text("There are users waiting to chat"),
                            new AcceptChatElement()
            ));
            case NO_CHAT_AVAILABLE -> obswap("chat-container", div().content(
                    div().text("There are no chat requests")
            ));
            default -> throw new IllegalStateException("Unexpected type: " + msg.getType());
        };
    }

    private HtmlElement scrollIntoView(HtmlElement content) {
        content.hxExt("simple-loaded-event");
        content.attr("hx-sle-onload", "this.scrollIntoView()");
        return content;
    }

    /**
     * Encodes a ChatProtoMessage into a byte array
     * @param message the message to encode
     * @return the byte array
     */
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
