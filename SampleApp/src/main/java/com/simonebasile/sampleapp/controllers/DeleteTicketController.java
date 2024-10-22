package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.HomeView;
import com.simonebasile.sampleapp.views.TicketNotFoundView;
import com.simonebasile.sampleapp.views.UserTicketsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;

public class DeleteTicketController extends MethodHandler<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(DeleteTicketController.class);
    private final TicketService ticketService;
    private final SessionService sessionService;
    private final UserService userService;

    public DeleteTicketController(SessionService sessionService, UserService userService, TicketService ticketService) {
        this.ticketService = ticketService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r.getVersion(), "/");
        }
        IdRequest id = FormHttpMapper.map(r.getBody(), IdRequest.class);
        if(ticketService.delete(id.getId(), user)) {
            final List<Ticket> byOwner = ticketService.getByOwner(user.getUsername());
            return ResponseUtils.fromView(r.getVersion(), new UserTicketsView(byOwner));
        } else {
            return ResponseUtils.fromView(r.getVersion(), 404, new TicketNotFoundView(id.getId()));
        }
    }
}
