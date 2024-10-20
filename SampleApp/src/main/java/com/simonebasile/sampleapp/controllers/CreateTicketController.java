package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.CreateTicketView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class CreateTicketController extends MethodHandler<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(CreateTicketController.class);
    private final SessionService sessionService;
    private final UserService userService;
    private final TicketService ticketService;


    public CreateTicketController(SessionService sessionService, UserService userService, TicketService ticketService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.ticketService = ticketService;
    }
    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        //Check role user
        return ResponseUtils.fromView(r.getVersion(), new CreateTicketView());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        //Check role user
        try {
            log.debug("Creating ticket {}", new String(r.getBody().readAllBytes()));
        } catch (IOException e) {}
        return handleGet(r);
    }
}
