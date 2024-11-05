package com.simonebasile.sampleapp.controllers.htmx;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.controllers.UnauthorizedPage;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.views.CreateTicketView;
import com.simonebasile.sampleapp.views.htmx.CreateTicketSection;
import com.simonebasile.sampleapp.views.htmx.UserTicketsSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class HTMXCreateTicketController extends MethodHandler<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(HTMXCreateTicketController.class);
    private final SessionService sessionService;
    private final UserService userService;
    private final TicketService ticketService;


    public HTMXCreateTicketController(SessionService sessionService, UserService userService, TicketService ticketService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        //Check role user
        return new HttpResponse<>(r.getVersion(), new CreateTicketSection());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return new HttpResponse<>(r.getVersion(), new UnauthorizedPage());
        }
        CreateTicket body = FormHttpMapper.map(r.getBody(), CreateTicket.class);
        String id;
        try {
            id = ticketService.createTicket(new Ticket(body), user).getId();
        } catch (CreateTicketException e) {
            return new HttpResponse<>(r.getVersion(), new CreateTicketSection(e.getMessage()));
        }
        //TODO replace with ticket detail
        final List<Ticket> byOwner = ticketService.getByOwner(user.getUsername());
        return new HttpResponse<>(r.getVersion(), new UserTicketsSection(byOwner));
    }
}
