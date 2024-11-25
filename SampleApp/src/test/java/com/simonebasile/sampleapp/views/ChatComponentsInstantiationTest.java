package com.simonebasile.sampleapp.views;

import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.views.chat.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChatComponentsInstantiationTest {

    private HttpResponseBody result;

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