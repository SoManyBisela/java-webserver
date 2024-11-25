package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.sampleapp.controller.employee.EmployeeTicketsController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class EmployeeTicketsControllerTest {

    private EmployeeTicketsController controller;
    private TicketService mockTicketService;
    private ApplicationRequestContext mockContext;

    @BeforeEach
    void setUp() {
        mockTicketService = mock(TicketService.class);
        controller = new EmployeeTicketsController(mockTicketService);
        mockContext = mock(ApplicationRequestContext.class);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/employee/tickets", HttpVersion.V1_1, new HttpHeaders(), null);
        List<Ticket> tickets = Collections.singletonList(mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "user123"));

        when(mockTicketService.getSubmitted()).thenReturn(tickets);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).getSubmitted();
    }
}