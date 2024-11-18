package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.assertions.UnreachableBranchException;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.EmployeeUpdateTicket;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.TicketNotFoundSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import com.simonebasile.sampleapp.views.EmployeeTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class TicketController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        if(user.getRole() == null || user.getRole() == Role.admin) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
        if(user.getRole() == Role.user) {
            Ticket ticket = ticketService.getById(id.getId(), user);
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket));
        } else if(user.getRole() == Role.employee) {
            Ticket ticket = ticketService.getById(id.getId(), user);
            if(ticket == null) {
                return new HttpResponse<>(r.getVersion(), 404, new TicketNotFoundSection(id.getId()));
            }
            return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user));
        }
        throw new UnreachableBranchException();
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        CreateTicket body = FormHttpMapper.map(r.getBody(), CreateTicket.class);
        String id;
        try {
            id = ticketService.createTicket(new Ticket(body), user).getId();
        } catch (CreateTicketException e) {
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(null));
        }
        Ticket t = ticketService.getById(id, user);
        return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(t));

    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Ticket ticket;
        if (user.getRole() == Role.user) {
            UserUpdateTicket body = FormHttpMapper.map(r.getBody(), UserUpdateTicket.class);
            try {
                ticket = ticketService.update(body, user);
            } catch (UpdateTicketException e) {
                ticket = ticketService.getById(body.getId(), user);
                return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket).errorMessage(e.getMessage()));
            }
            return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(ticket));
        } else if(user.getRole() == Role.employee) {
            EmployeeUpdateTicket body = FormHttpMapper.map(r.getBody(), EmployeeUpdateTicket.class);
            try {
                ticket = ticketService.update(body, user);
            } catch (UpdateTicketException e) {
                ticket = ticketService.getById(body.getId(), user);
                return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user, e.getMessage()));
            }
            return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user));
        } else {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        if(ticketService.delete(id.getId(), user)) {
            return new HttpResponse<>(r.getVersion(), null);
        } else {
            return new HttpResponse<>(r.getVersion(), new TicketNotFoundSection(id.getId()));
        }
    }
}
