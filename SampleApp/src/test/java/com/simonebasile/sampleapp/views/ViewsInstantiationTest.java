package com.simonebasile.sampleapp.views;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewsInstantiationTest {

    private HttpResponse.ResponseBody result;

    @Test
    void testAccountSectionInstantiation() {
        User user = new User("username", "password", Role.user);
        result = new AccountSection(user);
    }

    @Test
    void testAdminToolsSectionInstantiation() {
        result = new AdminToolsSection();
    }

    @Test
    void testEmployeeTicketDetailSectionInstantiation_toAssign() {
        Ticket ticket = mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "owner");
        User user = new User("username", "password", Role.employee);
        result = new EmployeeTicketDetailSection(ticket, user);
    }

    @Test
    void testEmployeeTicketDetailSectionInstantiation_assigned() {
        Ticket ticket = mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "owner");
        ticket.setAssignee("username");
        User user = new User("username", "password", Role.employee);
        result = new EmployeeTicketDetailSection(ticket, user);
    }

    @Test
    void testEmployeeTicketsSectionInstantiation() {
        List<Ticket> tickets = Collections.singletonList(mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "owner"));
        result = new EmployeeTicketsSection(tickets);
    }

    @Test
    void testHtmxViewInstantiation() {
        result = new HtmxView();
    }

    @Test
    void testLoginViewInstantiation() {
        result = new LoginView("Error message");
    }

    @Test
    void testMainViewInstantiation() {
        User user = new User("username", "password", Role.user);
        result = new MainView(user);
    }

    @Test
    void testNavbarButtonsInstantiation() {
        User user = new User("username", "password", Role.user);
        result = new NavbarButtons(user);
    }

    @Test
    void testRegisterViewInstantiation() {
        result = new RegisterView("Error message");
    }

    @Test
    void testTicketNotFoundSectionInstantiation() {
        result = new TicketNotFoundSection("1");
    }

    @Test
    void testUserTicketDetailSectionInstantiation_open() {
        Ticket ticket = mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "owner");
        result = new UserTicketDetailSection(ticket);
    }

    @Test
    void testUserTicketDetailSectionInstantiation_draft() {
        Ticket ticket = mkTicket("1", "Test Object", "Test Message", TicketState.DRAFT, "owner");
        result = new UserTicketDetailSection(ticket);
    }

    @Test
    void testUserTicketDetailSectionInstantiation_create() {
        result = new UserTicketDetailSection(null);
    }

    @Test
    void testUserTicketsSectionInstantiation() {
        List<Ticket> tickets = Collections.singletonList(mkTicket("1", "Test Object", "Test Message", TicketState.OPEN, "owner"));
        result = new UserTicketsSection(tickets);
    }

    @AfterEach
    void testResult() throws IOException {
        result.write(new ByteArrayOutputStream());
    }

    private static Ticket mkTicket(String id, String object, String message, TicketState state, String owner) {
        final Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setObject(object);
        ticket.setMessage(message);
        ticket.setState(state);
        ticket.setOwner(owner);
        var attachments = new ArrayList<Attachment>();
        attachments.add(new Attachment("path", "name"));
        attachments.add(new Attachment("path2", "name2"));
        ticket.setAttachments(attachments);
        var comments = new ArrayList<Comment>();
        comments.add(new Comment("author", "message", LocalDateTime.now()));
        comments.add(new Comment("author2", "message2", LocalDateTime.now()));
        ticket.setComments(comments);
        return ticket;

    }
}