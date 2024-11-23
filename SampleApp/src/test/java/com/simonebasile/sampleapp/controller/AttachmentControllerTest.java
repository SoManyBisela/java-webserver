package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.controller.user.AttachmentController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.AttachmentFile;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.UploadAttachmentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttachmentControllerTest {

    private AttachmentController controller;
    private TicketService mockTicketService;
    private ApplicationRequestContext mockContext;
    private User user;

    @BeforeEach
    void setUp() {
        mockTicketService = mock(TicketService.class);
        controller = new AttachmentController(mockTicketService);
        mockContext = mock(ApplicationRequestContext.class);
        user = new User("user123", "password", Role.user);
        when(mockContext.getLoggedUser()).thenReturn(user);
    }

    @Test
    void testHandlePost() {
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/attachments?ticketId=ticket123&filename=attachment.pdf", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[]{1,2,3}));
        Ticket ticket = mkTicket("ticket123", "Test Object", "Test Message", TicketState.OPEN, "user123");

        when(mockTicketService.getById("ticket123", user)).thenReturn(ticket);
        when(mockTicketService.uploadAttachment(any(Ticket.class), eq("attachment.pdf"), any(InputStream.class))).thenReturn(ticket);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).uploadAttachment(any(Ticket.class), eq("attachment.pdf"), any(InputStream.class));
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/attachments?ticketId=ticket123&ati=1", HttpVersion.V1_1, new HttpHeaders(), null);
        AttachmentFile file = new AttachmentFile("attachment.pdf", new File(""));

        when(mockTicketService.getAttachment("ticket123", 1, user)).thenReturn(file);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockTicketService, times(1)).getAttachment("ticket123", 1, user);
    }

    @Test
    void testHandleGet_NotFound() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/attachments?ticketId=ticket123&ati=1", HttpVersion.V1_1, new HttpHeaders(), null);

        when(mockTicketService.getAttachment("ticket123", 1, user)).thenReturn(null);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        verify(mockTicketService, times(1)).getAttachment("ticket123", 1, user);
    }

    @Test
    void testHandlePost_TicketNotFound() {
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/attachments?ticketId=ticket123&filename=attachment.pdf", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(mockTicketService.getById("ticket123", user)).thenReturn(null);

        assertThrows(UploadAttachmentException.class, () -> controller.handle(request, mockContext));
        verify(mockTicketService, times(1)).getById("ticket123", user);
    }

    @Test
    void testHandlePost_InvalidRequest() {
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/attachments?ticketId=ticket123&filename=", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[]{1, 2, 3}));
        Ticket ticket = new Ticket();

        when(mockTicketService.getById("ticket123", user)).thenReturn(ticket);

        assertThrows(UploadAttachmentException.class, () -> controller.handle(request, mockContext));
        verify(mockTicketService, times(1)).getById("ticket123", user);
    }

    @Test
    void testHandlePost_UploadException() {
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/attachments?ticketId=ticket123&filename=attachment.pdf", HttpVersion.V1_1, new HttpHeaders(), new ByteArrayInputStream(new byte[]{1, 2, 3}));
        Ticket ticket = new Ticket();

        when(mockTicketService.getById("ticket123", user)).thenReturn(ticket);
        when(mockTicketService.uploadAttachment(any(Ticket.class), eq("attachment.pdf"), any(InputStream.class))).thenThrow(new UploadAttachmentException("Upload failed"));

        assertThrows(ShowableException.class, () -> controller.handle(request, mockContext));
        verify(mockTicketService, times(1)).getById("ticket123", user);
        verify(mockTicketService, times(1)).uploadAttachment(any(Ticket.class), eq("attachment.pdf"), any(InputStream.class));
    }
}