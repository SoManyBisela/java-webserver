package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.WebsocketHandler;
import com.simonebasile.http.WebsocketMessage;
import com.simonebasile.http.WebsocketWriterImpl;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWsControllerTest {

    private ChatWsController controller;
    private WebsocketWriterImpl mockWriter;

    private User user;
    private ApplicationRequestContext userContext;
    private ChatWsController.WsState userWsState;

    private User employee;
    private ApplicationRequestContext employeeContext;
    private ChatWsController.WsState employeeWsState;

    @BeforeEach
    void setUp() {
        controller = new ChatWsController();
        mockWriter = mock(WebsocketWriterImpl.class);

        userContext = new ApplicationRequestContext();
        user = new User("user123", "password", Role.user);
        userContext.setLoggedUser(user);
        userWsState = controller.newContext(userContext);

        employeeContext = new ApplicationRequestContext();
        employee = new User("employee123", "password", Role.employee);
        employeeContext.setLoggedUser(employee);
        employeeWsState = controller.newContext(employeeContext);

    }

    @Test
    void testNewContext() {
        assertNotNull(userWsState);
        assertEquals(user, userWsState.getUser());
    }

    @Test
    void testOnServiceHandshake() {
        String[] availableService = {"chat"};
        ChatWsController.HandshakeResult result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Accept);
        assertEquals(result.protocol, "chat");
    }

    @Test
    void testDuplicatedConnections() throws IOException {
        String[] availableService = {"chat"};

        ChatWsController.HandshakeResult result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Accept);
        assertEquals(result.protocol, "chat");

        result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Accept);
        assertEquals(result.protocol, "chat");

        controller.onHandshakeComplete(mockWriter, userWsState);
        assertNotNull(userWsState.getWriter());
        verify(mockWriter, times(1)).sendTextBytes(any());

        controller.onHandshakeComplete(mockWriter, userWsState);
        assertNotNull(userWsState.getWriter());
        verify(mockWriter, times(1)).sendClose();

        result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Refuse);
    }

    @Test
    void testOnServiceHandshake_InvalidProtocol() {
        String[] availableService = {"invalid"};
        ChatWsController.HandshakeResult result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Refuse);
    }

    @Test
    void testOnServiceHandshake_notLogged() {
        String[] availableService = {"invalid"};
        ChatWsController.WsState unloggedState = controller.newContext(new ApplicationRequestContext());
        ChatWsController.HandshakeResult result = controller.onServiceHandshake(availableService, unloggedState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Refuse);
    }

    @Test
    void testOnServiceHandshake_NoProtocol() {
        String[] availableService = {};
        ChatWsController.HandshakeResult result = controller.onServiceHandshake(availableService, userWsState);
        assertSame(result.resultType, WebsocketHandler.HandshakeResultType.Accept);
        assertEquals(result.protocol, "chat");
    }

    @Test
    void testOnHandshakeComplete() throws IOException {
        controller.onHandshakeComplete(mockWriter, userWsState);
        assertNotNull(userWsState.getWriter());
        verify(mockWriter, never()).sendText(anyString());
    }

    @Test
    void testOnMessage() throws IOException {
        controller.onHandshakeComplete(mockWriter, userWsState);
        byte[][] bytes = new byte[1][];
        bytes[0] = """
                { "type": "SEND_MESSAGE", "sval": "Hello" }
                """.getBytes();

        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        verify(mockWriter, never()).sendClose();
    }

    @Test
    void testOnMessage_errors() {
        controller.onHandshakeComplete(mockWriter, userWsState);
        byte[][] bytes = new byte[1][];
        bytes[0] = new byte[]{ 0x00, 0x01, 0x02, 0x03 };

        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.BINARY);
        controller.onMessage(wsMessage, userWsState);
        verify(mockWriter, times(1)).sendClose();

        bytes[0] = """
            { "type": "ACCEPT_CHAT" }
            """.getBytes();

        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);
        verify(mockWriter, times(2)).sendClose();
    }

    @Test
    void testOnMessage_WantToChat() throws IOException {
        controller.onHandshakeComplete(mockWriter, userWsState);
        byte[][] bytes = new byte[1][];
        bytes[0] = """
            { "type": "WANT_TO_CHAT" }
            """.getBytes();

        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        verify(mockWriter, never()).sendClose();
    }

    @Test
    void testOnMessage_conversation() {
        controller.onHandshakeComplete(mockWriter, userWsState);
        controller.onHandshakeComplete(mockWriter, employeeWsState);
        byte[][] bytes = new byte[1][];

        bytes[0] = """
            { "type": "WANT_TO_CHAT" }
            """.getBytes();
        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "ACCEPT_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, employeeWsState);

        bytes[0] = """
            { "type": "SEND_MESSAGE", "sval": "Hello" }
            """.getBytes();

        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "END_CHAT" }
            """.getBytes();

        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "NEW_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        verify(mockWriter, never()).sendClose();
    }

    @Test
    void testOnMessage_stop_waiting() {
        controller.onHandshakeComplete(mockWriter, userWsState);
        controller.onHandshakeComplete(mockWriter, employeeWsState);
        byte[][] bytes = new byte[1][];

        bytes[0] = """
            { "type": "WANT_TO_CHAT" }
            """.getBytes();
        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "STOP_WAITING" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        verify(mockWriter, never()).sendClose();
    }


    @Test
    void testOnMessage_NewChat() throws IOException {
        controller.onHandshakeComplete(mockWriter, userWsState);
        controller.onHandshakeComplete(mockWriter, employeeWsState);
        byte[][] bytes = new byte[1][];

        bytes[0] = """
            { "type": "NEW_CHAT" }
            """.getBytes();
        WebsocketMessage wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, employeeWsState);

        bytes[0] = """
            { "type": "ACCEPT_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, employeeWsState);

        bytes[0] = """
            { "type": "WANT_TO_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "WANT_TO_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, userWsState);

        bytes[0] = """
            { "type": "NEW_CHAT" }
            """.getBytes();
        wsMessage = new WebsocketMessage(bytes, WebsocketMessage.MsgType.TEXT);
        controller.onMessage(wsMessage, employeeWsState);

        verify(mockWriter, never()).sendClose();
    }

    @Test
    void testOnClose() {
        controller.onHandshakeComplete(mockWriter, userWsState);
        controller.onClose(userWsState);
        assertNull(userWsState.getWriter().getConnectedTo());
    }
}