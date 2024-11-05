package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.assertions.UnreachableBranchException;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.EmployeeTicketDetail;
import com.simonebasile.sampleapp.views.TicketNotFoundView;
import com.simonebasile.sampleapp.views.UserTicketDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class TicketController extends MethodHandler<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);
    private final SessionService sessionService;
    private final UserService userService;
    private final TicketService ticketService;


    public TicketController(SessionService sessionService, UserService userService, TicketService ticketService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        if(user.getRole() == null || user.getRole() == Role.admin) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
        Ticket ticket = ticketService.getById(id.getId(), user);
        if(ticket == null) {
            return new HttpResponse<>(r.getVersion(), 404, new TicketNotFoundView(id.getId()));
        }
        if(user.getRole() == Role.user) {
            return new HttpResponse<>(r.getVersion(), new UserTicketDetail(ticket));
        } else if(user.getRole() == Role.employee) {
            return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetail(ticket, user));
        }
        throw new UnreachableBranchException();
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        Ticket ticket;
        if (user.getRole() == Role.user) {
            UserUpdateTicket body = FormHttpMapper.map(r.getBody(), UserUpdateTicket.class);
            try {
                ticket = ticketService.update(body, user);
            } catch (UpdateTicketException e) {
                ticket = ticketService.getById(body.getId(), user);
                return new HttpResponse<>(r.getVersion(), new UserTicketDetail(ticket, e.getMessage()));
            }
            return new HttpResponse<>(r.getVersion(), new UserTicketDetail(ticket));
        } else if(user.getRole() == Role.employee) {
            EmployeeUpdateTicket body = FormHttpMapper.map(r.getBody(), EmployeeUpdateTicket.class);
            try {
                ticket = ticketService.update(body, user);
            } catch (UpdateTicketException e) {
                ticket = ticketService.getById(body.getId(), user);
                return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetail(ticket, user, e.getMessage()));
            }
            return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetail(ticket, user));
        } else {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
    }

}