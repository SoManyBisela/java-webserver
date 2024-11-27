package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.server.WebServer;
import com.simonebasile.sampleapp.controller.*;
import com.simonebasile.sampleapp.controller.HomeController;
import com.simonebasile.sampleapp.controller.admin.AdminToolsController;
import com.simonebasile.sampleapp.controller.employee.EmployeeTicketController;
import com.simonebasile.sampleapp.controller.employee.EmployeeTicketsController;
import com.simonebasile.sampleapp.controller.user.AttachmentController;
import com.simonebasile.sampleapp.controller.user.UserTicketsController;
import com.simonebasile.sampleapp.controller.user.UserTicketController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.interceptors.AuthenticationInterceptor;
import com.simonebasile.sampleapp.interceptors.ErrorHandlingInterceptor;
import com.simonebasile.sampleapp.interceptors.ConditionalInterceptor;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.repository.TicketRepository;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.http.handlers.StaticFileHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class Main {

    public static void main(String[] args) {
        webapp();
    }

    private static void webapp() {

        //Mongo config
        var dbName = "AssistenzaDB";
        var userCollection = "users";
        var sessionCollection = "sessions";
        var ticketsCollection = "tickets";
        var pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));
        var mongoClient = MongoClients.create();
        var database = mongoClient.getDatabase(dbName);
        var usersColl = database.getCollection(userCollection, User.class).withCodecRegistry(pojoCodecRegistry);
        var sessionColl = database.getCollection(sessionCollection, SessionData.class).withCodecRegistry(pojoCodecRegistry);
        var ticketColl = database.getCollection(ticketsCollection, Ticket.class).withCodecRegistry(pojoCodecRegistry);

        //Repository config
        var userRepository = new UserRepository(usersColl);
        var sessionRepository = new SessionRepository(sessionColl);
        var ticketRepository = new TicketRepository(ticketColl);

        //Services config
        var sessionService = new SessionService(sessionRepository);
        var authenticationService = new AuthenticationService(userRepository, sessionService);
        var userService = new UserService(userRepository);
        var ticketService = new TicketService(ticketRepository, "uploads");

        //Controllers config
        var loginController = new LoginController(authenticationService);
        var logoutController = new LogoutController(sessionService);
        var registerController = new RegisterController(authenticationService);

        var homeController = new HomeController();
        var accountController = new AccountController(authenticationService);

        var adminToolsController = new AdminToolsController(authenticationService);

        var chatWsController = new ChatWsController();

        var attachmentController = new AttachmentController(ticketService);
        var userTicketsController = new UserTicketsController(ticketService);
        var userTicketController = new UserTicketController(ticketService);

        var employeeTicketsController = new EmployeeTicketsController(ticketService);
        var employeeTicketController = new EmployeeTicketController(ticketService);

        //Interceptor config
        var authInterceptor = new AuthenticationInterceptor<InputStream>(sessionService, userService);

        //Webserver config
        var webServer = WebServer.builder()
                .port(10131)
                .requestContextFactory(ApplicationRequestContext::new)
                .build();

        Predicate<HttpRequest<? extends InputStream>> skipAuth = (r) -> {
            String resource = r.getResource();
            return !resource.equals("/favicon.ico") &&
                    !resource.startsWith("/static") &&
                    !resource.equals("/register");
        };
        webServer.registerInterceptor((req, ctx, n) ->  {
            long start = System.currentTimeMillis();
            log.debug("Requets received {} {}", req.getMethod(), req.getResource());
            HttpResponse<? extends HttpResponseBody> res = n.handle(req, ctx);
            res.getHeaders().add("Connection", "Keep-Alive");
            log.debug("Response status: {}", res.getStatusCode());
            if(log.isDebugEnabled()) {
                log.debug("Response headers: ");
                HttpHeaders headers = res.getHeaders();
                for (Map.Entry<String, List<String>> entry : headers.entries()) {
                    log.debug("{}: {}", entry.getKey(), entry.getValue());
                }
            }
            log.info("Processing time: {}ms", System.currentTimeMillis() - start);
            return res;
        });
        webServer.registerInterceptor(new ErrorHandlingInterceptor());
        webServer.registerInterceptor(ConditionalInterceptor.fromPredicate(authInterceptor, skipAuth));
        webServer.registerHttpContext("/static", new StaticFileHandler<>("static-files"));
        webServer.registerHttpHandler("/login", loginController);
        webServer.registerHttpHandler("/logout", logoutController);
        webServer.registerHttpHandler("/register", registerController);

        webServer.registerHttpHandler("/", homeController);
        webServer.registerHttpHandler("/account", accountController);

        webServer.registerHttpHandler("/attachment", RoleBasedRouter.of(attachmentController, Role.user, Role.employee));
        webServer.registerHttpHandler("/tickets", RoleBasedRouter.builder()
                .handle(Role.user, userTicketsController)
                .handle(Role.employee, employeeTicketsController)
                .build());
        webServer.registerHttpHandler("/ticket", RoleBasedRouter.builder()
                .handle(Role.user, userTicketController)
                .handle(Role.employee, employeeTicketController)
                .build());

        webServer.registerWebSocketHandler("/chat", chatWsController);

        webServer.registerHttpHandler("/admin-tools", RoleBasedRouter.of(adminToolsController, Role.admin));


        try {
            webServer.start();
        } finally {
            mongoClient.close();
        }

    }
}
