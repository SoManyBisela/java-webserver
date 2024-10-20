package com.simonebasile.sampleapp.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.simonebasile.sampleapp.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketRepository {
    private final MongoCollection<Ticket> ticketCollection;

    public TicketRepository(MongoCollection<Ticket> ticketCollection) {
        this.ticketCollection = ticketCollection;
    }

    public List<Ticket> getByUser(String username) {
        return ticketCollection.find(Filters.eq("owner", username))
                .into(new ArrayList<>());
    }
}
