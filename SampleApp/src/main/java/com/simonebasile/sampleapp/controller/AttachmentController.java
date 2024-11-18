package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.FileResponseBody;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.DownloadAttachmentRequest;
import com.simonebasile.sampleapp.dto.UploadAttachmentRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.TicketNotFoundSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class AttachmentController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public AttachmentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        UploadAttachmentRequest uploadAttachmentRequest = FormHttpMapper.mapHttpResource(r.getResource(), UploadAttachmentRequest.class);
        String ticketId = uploadAttachmentRequest.getTicketId();
        Ticket ticket = ticketService.getById(ticketId, user);
        if(ticket == null) {
            log.warn("User {} Tried to upload attachment for ticket with id {}",user.getUsername(), ticketId);
            return new HttpResponse<>(r.getVersion(), new TicketNotFoundSection(ticketId));
        }

        if(!uploadAttachmentRequest.valid()) {
            log.warn("Invalid upload request {}", uploadAttachmentRequest);
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket)
                    .errorMessage("An unexpected error occurred while uploading the attachment"));
        }

        Path containerFolder = Path.of("uploads", ticketId);
        try {
            Files.createDirectories(containerFolder);
        } catch (IOException e) {
            log.error("Si è verificato un errore durante la creazione della cartella {} per gli allegati: {}",
                    containerFolder, e.getMessage(), e);
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket)
                    .errorMessage("An unexpected error occurred while uploading the attachment"));
        }
        Path file = containerFolder.resolve(UUID.randomUUID().toString());
        long transferred;
        try (final FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())){
            transferred = r.getBody().transferTo(fileOutputStream);
        } catch (Exception e) {
            log.error("Si è verificato un errore durante l'upload del file: {}",
                    e.getMessage(), e);
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket)
                    .errorMessage("An unexpected error occurred while uploading the attachment"));
        }
        if(transferred == 0) {
            log.warn("Il file caricato è vuoto");
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket)
                    .errorMessage("You cannot upload an empty file"));
        }

        ticket = ticketService.addAttachment(ticket, file.toString(), uploadAttachmentRequest.getFilename());
        return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket));
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        DownloadAttachmentRequest downloadReq = FormHttpMapper.mapHttpResource(r.getResource(), DownloadAttachmentRequest.class);
        Ticket t = ticketService.getById(downloadReq.getTicketId(), user);
        if(t == null) {
            final HttpHeaders headers = new HttpHeaders();
            return new HttpResponse<>(r.getVersion(), 404, headers, null);
        }
        Attachment attachment = t.getAttachments().get(downloadReq.getAti());
        final File file = new File(attachment.getPath());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + attachment.getName());
        return new HttpResponse<>(r.getVersion(), 200, headers, new FileResponseBody(file));
    }
}
