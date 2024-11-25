package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.AttachmentFile;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.repository.TicketRepository;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.service.errors.UploadAttachmentException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service that handles all tickets business logic.
 */
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final String uploadsFolder;

    public TicketService(TicketRepository ticketRepository, String uploadsFolder) {
        this.ticketRepository = ticketRepository;
        this.uploadsFolder = uploadsFolder;
    }

    /**
     * Get all tickets owned by a user.
     * @param username the username of the user
     * @return the list of tickets
     */
    public List<Ticket> getByOwner(String username) {
        return ticketRepository.getByOwner(username);
    }

    /**
     * Creates a new ticket.
     * checks if the object and the message are not empty.
     * @param body the ticket to create
     * @param user the user that creates the ticket
     * @return the created ticket
     */
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

    /**
     * Get a ticket by its id.
     * checks if the user is allowed to see the ticket.
     * @param id the id of the ticket
     * @param user the user that requests the ticket
     * @return the ticket or null if the user is not allowed to see it
     */
    public Ticket getById(String id, User user) {
        if(user.getRole() == Role.employee) {
            return ticketRepository.getSubmittedById(id);
        } else if(user.getRole() == Role.user) {
            return ticketRepository.getByIdAndOwner(id, user.getUsername());
        } else {
            return null;
        }
    }

    /**
     * Updates a ticket.
     * checks if the user is allowed to update the ticket.
     * if the ticket is in draft state, a request can update the object and the message and submit the ticket.
     * if the ticket is in open state, a request can add a comment.
     * @param body the ticket to update
     * @return the list of tickets
     */
    public Ticket update(UserUpdateTicket body, User user) {
        Ticket ticket = getById(body.getId(), user);
        if(ticket == null) {
            throw new UpdateTicketException("Ticket not found");
        }
        if(ticket.getState() == TicketState.CLOSED) {
            throw new UpdateTicketException("Ticket is closed");
        }
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

    /**
     * Updates a ticket.
     * checks if the user is allowed to update the ticket.
     * the employee can add a comment, assign the ticket to himself and close the ticket.
     * @param body the ticket to update
     * @return the list of tickets
     */
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

    /**
     * Utility method to add a comment to a ticket.
     */
    private static void addComment(Ticket ticket, User user, String content) {
        List<Comment> comments = ticket.getComments();
        if (comments == null) {
            comments = new ArrayList<>();
            ticket.setComments(comments);
        }
        comments.add(new Comment(user.getUsername(), content, LocalDateTime.now()));
    }

    /**
     * Get all submitted tickets.
     * @return the list of tickets
     */
    public List<Ticket> getSubmitted() {
        return ticketRepository.getSubmitted();
    }

    /**
     * Deletes a ticket.
     * checks if the user is allowed to delete the ticket.
     * @param id the id of the ticket
     * @param user the user that requests the deletion
     * @return true if the ticket was deleted, false if the user is not allowed to delete the ticket
     */
    public boolean delete(String id, User user) {
        Ticket ticket = getById(id, user);
        if(ticket != null) {
            ticketRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Uploads an attachment to a ticket.
     * files are stored in a folder named after the ticket id.
     * the name of the file is a random UUID.
     * if the file is empty, it is not uploaded.
     * @param ticket the ticket
     * @param filename the name of the file
     * @param body the content of the file
     * @return the updated ticket
     */
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

    /**
     * utility method to delete a file without throwing an exception.
     */
    private void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            log.error("An error occurred while deleting a an attachment: {}", e.getMessage(), e);
        }
    }

    /**
     * Get an attachment from a ticket.
     * @param ticketId the id of the ticket
     * @param attachmentIndex the index of the attachment
     * @param user the user that requests the attachment
     * @return the attachment or null if the user is not allowed to see it
     */
    public AttachmentFile getAttachment(String ticketId, int attachmentIndex, User user) {
        Ticket t = getById(ticketId, user);
        if(t == null) {
            return null;
        }
        final List<Attachment> attachments = t.getAttachments();
        if(attachmentIndex < 0 || attachmentIndex >= attachments.size()) {
            return null;
        }
        Attachment attachment = attachments.get(attachmentIndex);
        return new AttachmentFile(attachment.getName(), new File(attachment.getPath()));
    }
}
