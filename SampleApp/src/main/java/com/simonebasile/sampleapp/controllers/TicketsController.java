package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.base.Html5View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.EmployeeTicketsSection;
import com.simonebasile.sampleapp.views.UserTicketsSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class TicketsController extends MethodHandler<InputStream, ApplicationRequestContext> {
    private final TicketService ticketService;


    public TicketsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Role role = user.getRole();
        if(role == Role.user) {
            return userPage(r, user);
        } else if (role == Role.employee) {
            return employeePage(r, user);
        } else if(role == Role.admin) {
            return ResponseUtils.redirect(r, "/");
        } else {
            log.error("FATAL - User {} has unknown role {}", user.getUsername(), user.getRole());
            return errorPage(r);
        }
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> userPage(HttpRequest<?> r, User usr) {
        List<Ticket> tickets = ticketService.getByOwner(usr.getUsername());
        return new HttpResponse<>(r.getVersion(), new UserTicketsSection(tickets));
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> employeePage(HttpRequest<?> r, User usr) {
        List<Ticket> tickets = ticketService.getSubmitted();
        return new HttpResponse<>(r.getVersion(), new EmployeeTicketsSection(tickets));
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> errorPage(HttpRequest<?> r) {
        return new HttpResponse<>(r.getVersion(), 500, new HttpHeaders(),
                new Html5View().addContent(
                        HtmlElement.h(1).text("Error 500: Internal Server Error"),
                        HtmlElement.p().text("An unexpected error occurred, please contact the administrator")
                )
        );
    }

}
