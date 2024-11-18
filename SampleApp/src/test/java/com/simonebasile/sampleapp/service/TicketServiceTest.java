package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository mockTicketRepository;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        String path = "/files/attachment.pdf";
        String filename = "attachment.pdf";

        when(mockTicketRepository.update(ticket)).thenReturn(ticket);

        Ticket updatedTicket = ticketService.addAttachment(ticket, path, filename);

        assertEquals(1, updatedTicket.getAttachments().size());
        assertEquals(path, updatedTicket.getAttachments().get(0).getPath());
        assertEquals(filename, updatedTicket.getAttachments().get(0).getName());
    }

    private static Ticket newTicket(String id, String owner, TicketState state) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setOwner(owner);
        ticket.setState(state);
        return ticket;
    }
}
