package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.Ticket;
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

    public List<Ticket> getByUser(String username) {
        return ticketRepository.getByUser(username);
    }

    public Ticket createTicket(Ticket body, User user) {
        body.setId(UUID.randomUUID().toString());
        body.setOwner(user.getUsername());
        body.setState("OPEN");
        body.setAttachments(new ArrayList<>());
        ticketRepository.create(body);
        return body;
    }
}
