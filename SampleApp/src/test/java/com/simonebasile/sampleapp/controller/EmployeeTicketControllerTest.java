package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.controller.employee.EmployeeTicketController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmployeeTicketControllerTest {

    private EmployeeTicketController controller;
    private TicketService mockTicketService;
    private ApplicationRequestContext mockContext;
    private User employee;

    @BeforeEach
    void setUp() {
        mockTicketService = mock(TicketService.class);
        controller = new EmployeeTicketController(mockTicketService);
        mockContext = mock(ApplicationRequestContext.class);
        employee = new User("employee", "password", Role.employee);
        when(mockContext.getLoggedUser()).thenReturn(employee);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/employee/ticket?id=ticket123", HttpVersion.V1_1, new HttpHeaders(), null);
        Ticket ticket = mkTicket("ticket123", "Test Object", "Test Message", TicketState.OPEN, "user123");

        when(mockTicketService.getById("ticket123", employee)).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).getById("ticket123", employee);
    }

    @Test
    void testHandleGet_notFound() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/employee/ticket?id=ticket123", HttpVersion.V1_1, new HttpHeaders(), null);

        when(mockTicketService.getById("ticket123", employee)).thenReturn(null);

        assertThrows(ShowableException.class, () -> controller.handle(request, mockContext)) ;
        verify(mockTicketService, times(1)).getById("ticket123", employee);
    }

    @Test
    void testHandlePut() {
        String body = "id=ticket123&comment=New Comment&close=true&assign=true";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("PUT", "/employee/ticket", HttpVersion.V1_1, new HttpHeaders(), inputStream);
        Ticket ticket = mkTicket("ticket123", "Test Object", "Test Message", TicketState.OPEN, "user123");

        when(mockTicketService.update(any(EmployeeUpdateTicket.class), eq(employee))).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).update(any(EmployeeUpdateTicket.class), eq(employee));
    }

}