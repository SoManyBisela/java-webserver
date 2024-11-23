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

/**
 * Repository for managing tickets.
 */
public class TicketRepository {
    private final MongoCollection<Ticket> ticketCollection;

    public TicketRepository(MongoCollection<Ticket> ticketCollection) {
        this.ticketCollection = ticketCollection;
    }

    /**
     * Gets all tickets for a user.
     * @return the tickets
     */
    public List<Ticket> getByOwner(String username) {
        return ticketCollection.find(Filters.eq("owner", username))
                .into(new ArrayList<>());
    }

    /**
     * Creates a ticket.
     * @param body the ticket
     */
    public void create(Ticket body) {
        final InsertOneResult insertOneResult = ticketCollection.insertOne(body);
        body.setId(insertOneResult.getInsertedId().asString().getValue());
    }

    /**
     * Gets a ticket by id.
     * @param id the id
     * @return the ticket
     */
    public Ticket getById(String id) {
        return ticketCollection.find(Filters.eq("_id", id)).first();
    }

    /**
     * Gets a ticket by id and owner.
     * @param id the id
     * @param username the owner
     * @return the ticket
     */
    public Ticket getByIdAndOwner(String id, String username) {
        return ticketCollection.find(Filters.and(Filters.eq("_id", id), Filters.eq("owner", username))).first();
    }

    /**
     * Updates a ticket.
     * @param ticket the ticket
     * @return the updated ticket
     */
    public Ticket update(Ticket ticket) {
        ticketCollection.replaceOne(Filters.eq("_id", ticket.getId()), ticket);
        return ticket;
    }

    /**
     * Gets a ticket by id.
     * State must not be DRAFT.
     * @param id the id
     * @return the ticket
     */
    public Ticket getSubmittedById(String id) {
        return ticketCollection.find(Filters.and(
                Filters.eq("_id", id),
                Filters.not(Filters.eq("state", TicketState.DRAFT))
        )).first();
    }

    /**
     * Gets all submitted tickets.
     * @return the tickets
     */
    public List<Ticket> getSubmitted() {
        return ticketCollection.find(
                Filters.not(Filters.eq("state", TicketState.DRAFT))
        ).into(new ArrayList<>());
    }

    /**
     * Deletes a ticket by id.
     * @param id the id
     */
    public void deleteById(String id) {
        ticketCollection.deleteOne(Filters.eq("_id", id));
    }
}
