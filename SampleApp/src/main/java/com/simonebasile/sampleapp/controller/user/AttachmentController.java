package com.simonebasile.sampleapp.controller.user;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.FileResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.AttachmentFile;
import com.simonebasile.sampleapp.dto.DownloadAttachmentRequest;
import com.simonebasile.sampleapp.dto.UploadAttachmentRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.*;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.UploadAttachmentException;
import com.simonebasile.sampleapp.views.html.custom.AttachmentList;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Controller for the attachment section
 */
@Slf4j
public class AttachmentController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public AttachmentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Handles the POST request.
     * Uploads an attachment to a ticket.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        UploadAttachmentRequest uploadAttachmentRequest = FormHttpMapper.mapHttpResource(r.getResource(), UploadAttachmentRequest.class);
        if(uploadAttachmentRequest.getTicketId() == null) {
            throw new UploadAttachmentException("Missing ticket id");
        }
        Ticket ticket = ticketService.getById(uploadAttachmentRequest.getTicketId(), user);
        if(ticket == null) {
            throw new UploadAttachmentException("Ticket not found");
        }
        if(!uploadAttachmentRequest.valid()) {
            log.error("Invalid attachment request received");
            throw new UploadAttachmentException("An unexpected error occurred while uploading the attachment");
        }
        try {
            ticket = ticketService.uploadAttachment(ticket, uploadAttachmentRequest.getFilename(), r.getBody());
        } catch (UploadAttachmentException e) {
            throw new ShowableException(e);
        }
        return new HttpResponse<>(new AttachmentList(ticket.getAttachments(), ticket.getId()));
    }

    /**
     * Handles the GET request.
     * Downloads an attachment.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        DownloadAttachmentRequest downloadReq = FormHttpMapper.mapHttpResource(r.getResource(), DownloadAttachmentRequest.class);
        AttachmentFile file = ticketService.getAttachment(downloadReq.getTicketId(), downloadReq.getAti(), user);
        if(file == null) {
            return new HttpResponse<>(404, null);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + file.getName());
        return new HttpResponse<>(200, headers, new FileResponseBody(file.getContent()));
    }
}
