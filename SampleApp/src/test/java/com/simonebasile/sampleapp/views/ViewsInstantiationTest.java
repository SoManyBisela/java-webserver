package com.simonebasile.sampleapp.views;

import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.simonebasile.sampleapp.TestUtils.mkTicket;

class ViewsInstantiationTest {

    private HttpResponseBody result;

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

}