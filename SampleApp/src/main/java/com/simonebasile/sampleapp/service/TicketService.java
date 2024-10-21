package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.repository.TicketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getByOwner(String username) {
        return ticketRepository.getByOwner(username);
    }

    public Ticket createTicket(Ticket body, User user) {
        body.setId(UUID.randomUUID().toString());
        body.setOwner(user.getUsername());
        body.setState(TicketState.OPEN);
        body.setAttachments(new ArrayList<>());
        ticketRepository.create(body);
        return body;
    }

    public Ticket getById(String id, User user) {
        if(user.getRole() == Role.employee) {
            return ticketRepository.getById(id);
        } else if(user.getRole() == Role.user) {
            return ticketRepository.getByIdAndOwner(id, user.getUsername());
        } else {
            return null;
        }
    }

    public Ticket update(UserUpdateTicket body, User user) {
        return null;
    }

    public Ticket update(EmployeeUpdateTicket body, User user) {
        return null;
    }
}
