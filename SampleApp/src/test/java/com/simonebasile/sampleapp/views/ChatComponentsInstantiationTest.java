package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.chat.*;
import com.simonebasile.web.ssr.component.IHtmlElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


class ChatComponentsInstantiationTest {

    private IHtmlElement result;

    @AfterEach
    public void writeIt() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        result.write(byteArrayOutputStream);
        String res = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        System.out.println(res);
    }

    @Test
    void testAcceptChatElementInstantiation() {
        result = new AcceptChatElement();
    }

    @Test
    void testAlreadyConnectedSectionInstantiation() {
        result = new AlreadyConnectedSection();
    }

    @Test
    void testChatSectionInstantiation() {
        result = new ChatSection();
    }

    @Test
    void testEndChatElementInstantiation() {
        result = new EndChatElement();
    }

    @Test
    void testRestartChatElementInstantiation() {
        result = new RestartChatElement();
    }

    @Test
    void testSendMessageElementInstantiation() {
        result = new SendMessageElement();
    }

    @Test
    void testStopWaitingElementInstantiation() {
        result = new StopWaitingElement();
    }

    @Test
    void testWantToChatElementInstantiation() {
        result = new WantToChatElement();
    }

    @AfterEach
    void testResult() throws IOException {
        result.write(new ByteArrayOutputStream());
    }
}