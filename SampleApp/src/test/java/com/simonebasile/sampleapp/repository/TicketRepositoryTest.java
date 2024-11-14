package com.simonebasile.sampleapp.repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import org.bson.BsonString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketRepositoryTest {

    @Mock
    private MongoCollection<Ticket> mockTicketCollection;

    @InjectMocks
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetByOwner() {
        String username = "user123";
        ArrayList<Ticket> expectedTickets = new ArrayList<>();
        Ticket expectedTicket = new Ticket();
        expectedTicket.setId("ticket1");
        expectedTicket.setOwner(username);
        expectedTicket.setState(TicketState.OPEN);
        expectedTickets.add(expectedTicket);

        FindIterable<Ticket> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.into(anyList())).thenReturn(expectedTickets);
        when(mockTicketCollection.find(Filters.eq("owner", username))).thenReturn(mockFindIterable);

        List<Ticket> actualTickets = ticketRepository.getByOwner(username);

        assertNotNull(actualTickets);
        assertEquals(expectedTickets, actualTickets);
        verify(mockTicketCollection).find(Filters.eq("owner", username));
    }

    @Test
    void testCreate() {
        Ticket ticket = new Ticket();
        ticket.setOwner("user123");

        InsertOneResult mockInsertResult = mock(InsertOneResult.class);
        when(mockInsertResult.getInsertedId()).thenReturn(new BsonString("ticket1"));
        when(mockTicketCollection.insertOne(ticket)).thenReturn(mockInsertResult);

        ticketRepository.create(ticket);

        verify(mockTicketCollection).insertOne(ticket);
        assertEquals("ticket1", ticket.getId());
    }

    @Test
    void testGetById() {
        String ticketId = "ticket1";
        Ticket expectedTicket = new Ticket();
        expectedTicket.setId(ticketId);

        FindIterable<Ticket> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(expectedTicket);
        when(mockTicketCollection.find(Filters.eq("_id", ticketId))).thenReturn(mockFindIterable);

        Ticket actualTicket = ticketRepository.getById(ticketId);

        assertNotNull(actualTicket);
        assertEquals(expectedTicket, actualTicket);
        verify(mockTicketCollection).find(Filters.eq("_id", ticketId));
    }

    @Test
    void testGetByIdAndOwner() {
        String ticketId = "ticket1";
        String username = "user123";
        Ticket expectedTicket = new Ticket();
        expectedTicket.setId(ticketId);
        expectedTicket.setOwner(username);

        FindIterable<Ticket> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(expectedTicket);
        when(mockTicketCollection.find(Filters.and(Filters.eq("_id", ticketId), Filters.eq("owner", username))))
                .thenReturn(mockFindIterable);

        Ticket actualTicket = ticketRepository.getByIdAndOwner(ticketId, username);

        assertNotNull(actualTicket);
        assertEquals(expectedTicket, actualTicket);
        verify(mockTicketCollection).find(Filters.and(Filters.eq("_id", ticketId), Filters.eq("owner", username)));
    }

    @Test
    void testUpdate() {
        Ticket ticket = new Ticket();
        ticket.setId("ticket1");
        ticket.setOwner("user123");
        ticket.setState(TicketState.OPEN);

        ticketRepository.update(ticket);

        verify(mockTicketCollection).replaceOne(Filters.eq("_id", ticket.getId()), ticket);
    }

    @Test
    void testGetSubmittedById() {
        String ticketId = "ticket1";
        Ticket expectedTicket = new Ticket();
        expectedTicket.setId(ticketId);
        expectedTicket.setState(TicketState.OPEN);

        FindIterable<Ticket> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(expectedTicket);
        when(mockTicketCollection.find(Filters.and(Filters.eq("_id", ticketId),
                Filters.not(Filters.eq("state", TicketState.DRAFT))))).thenReturn(mockFindIterable);

        Ticket actualTicket = ticketRepository.getSubmittedById(ticketId);

        assertNotNull(actualTicket);
        assertEquals(expectedTicket, actualTicket);
        verify(mockTicketCollection).find(Filters.and(Filters.eq("_id", ticketId),
                Filters.not(Filters.eq("state", TicketState.DRAFT))));
    }

    @Test
    void testGetSubmitted() {
        ArrayList<Ticket> expectedTickets = new ArrayList<>();
        Ticket ticket1 = new Ticket();
        ticket1.setId("ticket1");
        ticket1.setState(TicketState.OPEN);
        expectedTickets.add(ticket1);

        FindIterable<Ticket> mockFindIterable = mock(FindIterable.class);
        when(mockFindIterable.into(anyList())).thenReturn( expectedTickets);
        when(mockTicketCollection.find(Filters.not(Filters.eq("state", TicketState.DRAFT)))).thenReturn(mockFindIterable);

        List<Ticket> actualTickets = ticketRepository.getSubmitted();

        assertNotNull(actualTickets);
        assertEquals(expectedTickets, actualTickets);
        verify(mockTicketCollection).find(Filters.not(Filters.eq("state", TicketState.DRAFT)));
    }

    @Test
    void testDeleteById() {
        String ticketId = "ticket1";

        ticketRepository.deleteById(ticketId);

        verify(mockTicketCollection).deleteOne(Filters.eq("_id", ticketId));
    }
}
