package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.repository.TicketRepository;

import java.util.List;

public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getByUser(String username) {
        return ticketRepository.getByUser(username);
    }
}
