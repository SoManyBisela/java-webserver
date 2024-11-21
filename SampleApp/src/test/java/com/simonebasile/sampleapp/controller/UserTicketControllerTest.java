package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.controller.user.UserTicketController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserTicketControllerTest {

    private UserTicketController controller;
    private TicketService mockTicketService;
    private ApplicationRequestContext mockContext;
    private User user;

    @BeforeEach
    void setUp() {
        mockTicketService = mock(TicketService.class);
        controller = new UserTicketController(mockTicketService);
        mockContext = mock(ApplicationRequestContext.class);
        user = new User("user123", "password", Role.user);
        when(mockContext.getLoggedUser()).thenReturn(user);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/user/ticket?id=ticket123", HttpVersion.V1_1, new HttpHeaders(), null);
        Ticket ticket = mkTicket("ticket123", "Test Object", "Test Message", TicketState.OPEN, "user123");

        when(mockTicketService.getById("ticket123", user)).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).getById("ticket123", user);
    }

    @Test
    void testHandlePost() {
        String body = "object=Test Object&message=Test Message";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/user/ticket", HttpVersion.V1_1, new HttpHeaders(), inputStream);
        CreateTicket createTicket = new CreateTicket("Test Object", "Test Message");
        Ticket ticket = new Ticket(createTicket);

        when(mockTicketService.createTicket(any(Ticket.class), eq(user))).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).createTicket(any(Ticket.class), eq(user));
    }

    @Test
    void testHandlePut() {
        String body = "id=ticket123&object=Updated Object&message=Updated Message";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("PUT", "/user/ticket", HttpVersion.V1_1, new HttpHeaders(), inputStream);
        Ticket ticket = mkTicket("ticket123", "Updated Object", "Updated Message", TicketState.OPEN, "user123");

        when(mockTicketService.update(any(UserUpdateTicket.class), eq(user))).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).update(any(UserUpdateTicket.class), eq(user));
    }

    @Test
    void testHandleDelete() {
        HttpRequest<InputStream> request = new HttpRequest<>("DELETE", "/user/ticket?id=ticket123", HttpVersion.V1_1, new HttpHeaders(), null);

        when(mockTicketService.delete("ticket123", user)).thenReturn(true);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        verify(mockTicketService, times(1)).delete("ticket123", user);
    }
}