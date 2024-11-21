package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.repository.TicketRepository;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.service.errors.UploadAttachmentException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository mockTicketRepository;

    private TicketService ticketService;

    private final String uploadsFolder = "testUploads";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketService(mockTicketRepository, uploadsFolder);
    }

    @AfterEach
    void removeUploadsFolder() throws Exception {
        final Path uploads = Path.of(uploadsFolder);
        if (Files.exists(uploads)) {
            final ArrayList<Path> toRemove = new ArrayList<>();
            try(var files = Files.walk(uploads)) {
                files.forEach(path -> {
                    if(Files.isRegularFile(path)) {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if(Files.isDirectory(path)) {
                        toRemove.add(path);
                    }
                });
            }
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                Files.delete(toRemove.get(i));
            }
        }
    }

    @Test
    void testGetByOwner() {
        String username = "user123";
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(newTicket("ticket1", username, TicketState.OPEN));

        when(mockTicketRepository.getByOwner(username)).thenReturn(tickets);

        List<Ticket> result = ticketService.getByOwner(username);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ticket1", result.get(0).getId());
    }

    @Test
    void testCreateTicket() {
        User user = new User("user123", "password", Role.user);
        Ticket ticket = new Ticket(new CreateTicket("object", "message"));

        Ticket createdTicket = ticketService.createTicket(ticket, user);

        assertNotNull(createdTicket.getId());
        assertEquals(user.getUsername(), createdTicket.getOwner());
        assertEquals(TicketState.DRAFT, createdTicket.getState());
        assertTrue(createdTicket.getAttachments().isEmpty());
        verify(mockTicketRepository).create(ticket);
    }

    @Test
    void testGetById_AsEmployee_SubmittedTicket() {
        User employee = new User("employee", "password", Role.employee);
        String ticketId = "ticket123";
        Ticket ticket = newTicket(ticketId, "user123", TicketState.OPEN);

        when(mockTicketRepository.getSubmittedById(ticketId)).thenReturn(ticket);

        Ticket result = ticketService.getById(ticketId, employee);

        assertNotNull(result);
        assertEquals(ticketId, result.getId());
    }

    @Test
    void testGetById_AsUser_OwnTicket() {
        User user = new User("user123", "password", Role.user);
        String ticketId = "ticket123";
        Ticket ticket = newTicket(ticketId, user.getUsername(), TicketState.OPEN);

        when(mockTicketRepository.getByIdAndOwner(ticketId, user.getUsername())).thenReturn(ticket);

        Ticket result = ticketService.getById(ticketId, user);

        assertNotNull(result);
        assertEquals(ticketId, result.getId());
    }

    @Test
    void testUpdate_UserDraftTicket() {
        User user = new User("user123", "password", Role.user);
        UserUpdateTicket updateRequest = new UserUpdateTicket("ticket123", "New Object", "New Message", null, true);
        Ticket ticket = newTicket("ticket123", user.getUsername(), TicketState.DRAFT);

        when(mockTicketRepository.getByIdAndOwner(updateRequest.getId(), user.getUsername())).thenReturn(ticket);
        when(mockTicketRepository.update(ticket)).thenReturn(ticket);

        Ticket updatedTicket = ticketService.update(updateRequest, user);

        assertEquals("New Object", updatedTicket.getObject());
        assertEquals("New Message", updatedTicket.getMessage());
        assertEquals(TicketState.OPEN, updatedTicket.getState());
    }

    @Test
    void testUpdate_EmployeeUpdateTicket() {
        User employee = new User("employee", "password", Role.employee);
        EmployeeUpdateTicket updateRequest = new EmployeeUpdateTicket("ticket123", "Adding a comment", true, true);
        Ticket ticket = newTicket("ticket123", "user123", TicketState.OPEN);

        when(mockTicketRepository.getSubmittedById(updateRequest.getId())).thenReturn(ticket);
        when(mockTicketRepository.update(ticket)).thenReturn(ticket);

        Ticket updatedTicket = ticketService.update(updateRequest, employee);

        assertEquals(employee.getUsername(), updatedTicket.getAssignee());
        assertEquals(TicketState.CLOSED, updatedTicket.getState());
        assertEquals(1, updatedTicket.getComments().size());
    }

    @Test
    void testDelete_ExistingTicket() {
        User user = new User("user123", "password", Role.user);
        String ticketId = "ticket123";
        Ticket ticket = newTicket(ticketId, user.getUsername(), TicketState.OPEN);

        when(mockTicketRepository.getByIdAndOwner(ticketId, user.getUsername())).thenReturn(ticket);

        boolean result = ticketService.delete(ticketId, user);

        assertTrue(result);
        verify(mockTicketRepository).deleteById(ticketId);
    }

    @Test
    void testDelete_NonExistingTicket() {
        User user = new User("user123", "password", Role.user);
        String ticketId = "ticketNotExist";

        when(mockTicketRepository.getByIdAndOwner(ticketId, user.getUsername())).thenReturn(null);

        boolean result = ticketService.delete(ticketId, user);

        assertFalse(result);
        verify(mockTicketRepository, never()).deleteById(ticketId);
    }

    @Test
    void testAddAttachment() {
        Ticket ticket = newTicket("ticket123", "user123", TicketState.OPEN);
        ticket.setAttachments(new ArrayList<>());
        String filename = "attachment.pdf";

        when(mockTicketRepository.update(ticket)).thenReturn(ticket);

        Ticket updatedTicket = ticketService.uploadAttachment(ticket, filename, new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5}));

        assertEquals(1, updatedTicket.getAttachments().size());
        assertEquals(filename, updatedTicket.getAttachments().get(0).getName());
        final String path = updatedTicket.getAttachments().get(0).getPath();
        assertTrue(Files.exists(Path.of(path)));
    }

    @Test
    void testCreateTicket_EmptyObject() {
        User user = new User("user123", "password", Role.user);
        Ticket ticket = new Ticket();
        ticket.setMessage("message");

        Exception exception = assertThrows(CreateTicketException.class, () -> ticketService.createTicket(ticket, user));

        assertEquals("Object cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateTicket_EmptyMessage() {
        User user = new User("user123", "password", Role.user);
        Ticket ticket = new Ticket();
        ticket.setObject("object");

        Exception exception = assertThrows(CreateTicketException.class, () -> ticketService.createTicket(ticket, user));

        assertEquals("Message cannot be empty", exception.getMessage());
    }

    @Test
    void testUpdate_UserDraftTicket_EmptyObject() {
        User user = new User("user123", "password", Role.user);
        UserUpdateTicket updateRequest = new UserUpdateTicket("ticket123", "", "New Message", null, true);
        Ticket ticket = newTicket("ticket123", user.getUsername(), TicketState.DRAFT);

        when(mockTicketRepository.getByIdAndOwner(updateRequest.getId(), user.getUsername())).thenReturn(ticket);

        Exception exception = assertThrows(UpdateTicketException.class, () -> ticketService.update(updateRequest, user));

        assertEquals("Object cannot be empty", exception.getMessage());
    }

    @Test
    void testUpdate_UserDraftTicket_EmptyMessage() {
        User user = new User("user123", "password", Role.user);
        UserUpdateTicket updateRequest = new UserUpdateTicket("ticket123", "New Object", "", null, true);
        Ticket ticket = newTicket("ticket123", user.getUsername(), TicketState.DRAFT);

        when(mockTicketRepository.getByIdAndOwner(updateRequest.getId(), user.getUsername())).thenReturn(ticket);

        Exception exception = assertThrows(UpdateTicketException.class, () -> ticketService.update(updateRequest, user));

        assertEquals("Message cannot be empty", exception.getMessage());
    }

    @Test
    void testUpdate_EmployeeUpdateTicket_EmptyComment() {
        User employee = new User("employee", "password", Role.employee);
        EmployeeUpdateTicket updateRequest = new EmployeeUpdateTicket("ticket123", "", true, true);
        Ticket ticket = newTicket("ticket123", "user123", TicketState.OPEN);

        when(mockTicketRepository.getSubmittedById(updateRequest.getId())).thenReturn(ticket);

        Exception exception = assertThrows(UpdateTicketException.class, () -> ticketService.update(updateRequest, employee));

        assertEquals("Comment cannot be empty", exception.getMessage());
    }

    @Test
    void testAddComment() {
        User user = new User("user123", "password", Role.user);
        UserUpdateTicket updateRequest = new UserUpdateTicket("ticket123", null, null, "New Comment", false);
        Ticket ticket = newTicket("ticket123", user.getUsername(), TicketState.OPEN);

        when(mockTicketRepository.getByIdAndOwner(updateRequest.getId(), user.getUsername())).thenReturn(ticket);
        when(mockTicketRepository.update(ticket)).thenReturn(ticket);

        Ticket updatedTicket = ticketService.update(updateRequest, user);

        assertNotNull(updatedTicket.getComments());
        assertEquals(1, updatedTicket.getComments().size());
        assertEquals("New Comment", updatedTicket.getComments().get(0).getContent());
        assertEquals(user.getUsername(), updatedTicket.getComments().get(0).getAuthor());
    }

    @Test
    void testAddComment_EmptyComment() {
        User user = new User("user123", "password", Role.user);
        UserUpdateTicket updateRequest = new UserUpdateTicket("ticket123", null, null, "", false);
        Ticket ticket = newTicket("ticket123", user.getUsername(), TicketState.OPEN);

        when(mockTicketRepository.getByIdAndOwner(updateRequest.getId(), user.getUsername())).thenReturn(ticket);

        Exception exception = assertThrows(UpdateTicketException.class, () -> ticketService.update(updateRequest, user));

        assertEquals("Comment cannot be empty", exception.getMessage());
    }

    @Test
    void testUploadEmptyAttachment() {
        Ticket ticket = newTicket("ticket123", "user123", TicketState.OPEN);
        String filename = "empty.pdf";

        Exception exception = assertThrows(UploadAttachmentException.class, () -> ticketService.uploadAttachment(ticket, filename, new ByteArrayInputStream(new byte[0])));

        assertEquals("Cannot upload an empty attachment", exception.getMessage());
    }

    private static Ticket newTicket(String id, String owner, TicketState state) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setOwner(owner);
        ticket.setState(state);
        return ticket;
    }
}
