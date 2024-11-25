package com.simonebasile.sampleapp.controller.user;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.UserTicketsSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

/**
 * Controller for the user tickets section
 */
@Slf4j
public class UserTicketsController extends MethodHandler<InputStream, ApplicationRequestContext> {
    private final TicketService ticketService;


    public UserTicketsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Handles the GET request.
     * Renders the tickets section with all the tickets owned by the logged user.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        List<Ticket> tickets = ticketService.getByOwner(context.getLoggedUser().getUsername());
        return new HttpResponse<>(new UserTicketsSection(tickets));
    }
}
