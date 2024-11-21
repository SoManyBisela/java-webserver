package com.simonebasile.sampleapp.service;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.UploadAttachmentRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.repository.TicketRepository;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.service.errors.UploadAttachmentException;
import com.simonebasile.sampleapp.views.TicketNotFoundSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final String uploadsFolder;

    public TicketService(TicketRepository ticketRepository, String uploadsFolder) {
        this.ticketRepository = ticketRepository;
        this.uploadsFolder = uploadsFolder;
    }

    public List<Ticket> getByOwner(String username) {
        return ticketRepository.getByOwner(username);
    }

    public Ticket createTicket(Ticket body, User user) {
        if(Utils.isEmpty(body.getObject())) {
            throw new CreateTicketException("Object cannot be empty");
        }
        if(Utils.isEmpty(body.getMessage())) {
            throw new CreateTicketException("Message cannot be empty");
        }
        body.setId(UUID.randomUUID().toString());
        body.setOwner(user.getUsername());
        body.setState(TicketState.DRAFT);
        body.setAttachments(new ArrayList<>());
        body.setCreationDate(LocalDateTime.now());
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
                if(Utils.isEmpty(body.getObject())) {
                    throw new UpdateTicketException("Object cannot be empty");
                }
                ticket.setObject(body.getObject());
            }
            if(body.getMessage() != null) {
                if(Utils.isEmpty(body.getMessage())) {
                    throw new UpdateTicketException("Message cannot be empty");
                }
                ticket.setMessage(body.getMessage());
            }
            if(body.isSubmit()) {
                ticket.setState(TicketState.OPEN);
                ticket.setSubmissionDate(LocalDateTime.now());
            }
        } else {
            if(body.getComment() != null) {
                if(Utils.isEmpty(body.getComment())) {
                    throw new UpdateTicketException("Comment cannot be empty");
                }
                addComment(ticket, user, body.getComment());
            }
        }
        return ticketRepository.update(ticket);
    }

    public Ticket update(EmployeeUpdateTicket body, User user) {
        Ticket ticket = getById(body.getId(), user);
        if(body.getComment() != null) {
            if(Utils.isEmpty(body.getComment())) {
                throw new UpdateTicketException("Comment cannot be empty");
            }
            addComment(ticket, user, body.getComment());
        }
        if(body.isAssign()) {
            ticket.setAssignee(user.getUsername());
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
        comments.add(new Comment(user.getUsername(), content, LocalDateTime.now()));
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

    public Ticket uploadAttachment(Ticket ticket, String filename, InputStream body) {
        Path containerFolder = Path.of(uploadsFolder, ticket.getId());
        try {
            Files.createDirectories(containerFolder);
        } catch (IOException e) {
            log.error("An error occurred while uploading the attachment: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Path file = containerFolder.resolve(UUID.randomUUID().toString());
        long transferred;
        try (final FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())){
            transferred = body.transferTo(fileOutputStream);
        } catch (Exception e) {
            log.error("An error occurred while uploading the attachment: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        if(transferred == 0) {
            log.warn("Il file caricato è vuoto");
            deleteFile(file);
            throw new UploadAttachmentException("Cannot upload an empty attachment");
        }
        ticket.getAttachments().add(new Attachment(file.toString(), filename));
        try {
            return ticketRepository.update(ticket);
        } catch (Exception e) {
            deleteFile(file);
            log.error("An error occurred while uploading the attachment: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            log.error("An error occurred while deleting a an attachment: {}", e.getMessage(), e);
        }
    }
}
