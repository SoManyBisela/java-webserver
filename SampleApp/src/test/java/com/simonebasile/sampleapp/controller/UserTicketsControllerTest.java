package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.controller.user.UserTicketsController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.UserTicketsSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class UserTicketsControllerTest {

    private UserTicketsController controller;
    private TicketService mockTicketService;
    private ApplicationRequestContext mockContext;
    private User user;

    @BeforeEach
    void setUp() {
        mockTicketService = mock(TicketService.class);
        controller = new UserTicketsController(mockTicketService);
        mockContext = mock(ApplicationRequestContext.class);
        user = new User("user123", "password", Role.user);
        when(mockContext.getLoggedUser()).thenReturn(user);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/user/tickets", HttpVersion.V1_1, new HttpHeaders(), null);
        List<Ticket> tickets = Collections.singletonList(mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "user123"));

        when(mockTicketService.getByOwner(user.getUsername())).thenReturn(tickets);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).getByOwner(user.getUsername());
    }
}