package com.simonebasile.sampleapp.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository {
    private final MongoCollection<Ticket> ticketCollection;

    public TicketRepository(MongoCollection<Ticket> ticketCollection) {
        this.ticketCollection = ticketCollection;
    }

    public List<Ticket> getByOwner(String username) {
        return ticketCollection.find(Filters.eq("owner", username))
                .into(new ArrayList<>());
    }

    public void create(Ticket body) {
        final InsertOneResult insertOneResult = ticketCollection.insertOne(body);
        body.setId(insertOneResult.getInsertedId().asString().getValue());
    }

    public Ticket getById(String id) {
        return ticketCollection.find(Filters.eq("_id", id)).first();
    }

    public Ticket getByIdAndOwner(String id, String username) {
        return ticketCollection.find(Filters.and(Filters.eq("_id", id), Filters.eq("owner", username))).first();
    }

    public Ticket update(Ticket ticket) {
        ticketCollection.replaceOne(Filters.eq("_id", ticket.getId()), ticket);
        return ticket;
    }

    public Ticket getSubmittedById(String id) {
        return ticketCollection.find(Filters.and(
                Filters.eq("_id", id),
                Filters.not(Filters.eq("state", TicketState.DRAFT))
        )).first();
    }

    public List<Ticket> getSubmitted() {
        return ticketCollection.find(
                Filters.not(Filters.eq("state", TicketState.DRAFT))
        ).into(new ArrayList<>());
    }

    public void deleteById(String id) {
        ticketCollection.deleteOne(Filters.eq("_id", id));
    }
}
