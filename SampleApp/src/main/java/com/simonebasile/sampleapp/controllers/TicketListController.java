package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.EmployeePageView;
import com.simonebasile.sampleapp.views.UserTicketsView;
import com.simonebasile.sampleapp.views.base.BaseView;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class TicketListController extends MethodHandler<InputStream> {
    private static final Logger log = LoggerFactory.getLogger(TicketListController.class);
    private final SessionService sessionService;
    private final UserService userService;
    private final TicketService ticketService;


    public TicketListController(SessionService sessionService, UserService userService, TicketService ticketService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        String role = user.getRole();
        if("user".equals(role)) {
            return userPage(r, user);
        } else if ("employee".equals(role)) {
            return employeePage(r, user);
        } else if("admin".equals(role)) {
            return ResponseUtils.redirect(r.getVersion(), "/");
        } else {
            log.error("FATAL - User {} has unknown role {}", user.getUsername(), user.getRole());
            return errorPage(r);
        }
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> userPage(HttpRequest<?> r, User usr) {
        List<Ticket> tickets = ticketService.getByOwner(usr.getUsername());
        return ResponseUtils.fromView(r.getVersion(), new UserTicketsView(tickets));
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> employeePage(HttpRequest<?> r, User usr) {
        return ResponseUtils.fromView(r.getVersion(), new EmployeePageView());
    }

    private HttpResponse<? extends HttpResponse.ResponseBody> errorPage(HttpRequest<?> r) {
        return new HttpResponse<>(r.getVersion(), 500, new HttpHeaders(),
                new BaseView().addContent(
                        HtmlElement.h(1).text("Error 500: Internal Server Error"),
                        HtmlElement.p().text("An unexpected error occurred, please contact the administrator")
                )
        );
    }

}
