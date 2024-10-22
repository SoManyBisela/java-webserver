package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.*;
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
        body.setState(TicketState.DRAFT);
        body.setAttachments(new ArrayList<>());
        ticketRepository.create(body);
        return body;
    }

    public Ticket getById(String id, User user) {
        if(user.getRole() == Role.employee) {
            return ticketRepository.getSubmittedById(id);
        } else if(user.getRole() == Role.user) {
            return ticketRepository.getByIdAndOwner(id, user.getUsername());
        } else {
            return null;
        }
    }

    public Ticket update(UserUpdateTicket body, User user) {
        Ticket ticket = getById(body.getId(), user);
        if(ticket.getState() == TicketState.DRAFT) {
            if(body.getObject() != null) {
                ticket.setObject(body.getObject());
            }
            if(body.getMessage() != null) {
                ticket.setMessage(body.getMessage());
            }
            if(body.isSubmit()) {
                ticket.setState(TicketState.OPEN);
            }
        } else {
            if(body.getComment() != null) {
                addComment(ticket, user, body.getComment());
            }
        }
        return ticketRepository.update(ticket);
    }

    public Ticket update(EmployeeUpdateTicket body, User user) {
        Ticket ticket = getById(body.getId(), user);
        if(body.getComment() != null) {
            addComment(ticket, user, body.getComment());
        }
        if(body.isAssign()) {
            ticket.setAssegnee(user.getUsername());
        }
        if(ticket.getState() == TicketState.OPEN && body.isClose()) {
            ticket.setState(TicketState.CLOSED);
        }
        return ticketRepository.update(ticket);
    }

    private static void addComment(Ticket ticket, User user, String content) {
        List<Comment> comments = ticket.getComments();
        if (comments == null) {
            comments = new ArrayList<>();
            ticket.setComments(comments);
        }
        comments.add(new Comment(user.getUsername(), content));
    }


    public List<Ticket> getSubmitted() {
        return ticketRepository.getSubmitted();
    }

    public boolean delete(String id, User user) {
        Ticket ticket = getById(id, user);
        if(ticket != null) {
            ticketRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
