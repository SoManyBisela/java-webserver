package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.simonebasile.http.*;
import com.simonebasile.sampleapp.chat.MessageDispatcher;
import com.simonebasile.sampleapp.controllers.*;
import com.simonebasile.sampleapp.controllers.HomeController;
import com.simonebasile.sampleapp.interceptors.AuthenticationInterceptor;
import com.simonebasile.sampleapp.interceptors.InterceptorSkip;
import com.simonebasile.sampleapp.interceptors.SessionInterceptor;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.repository.TicketRepository;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.handlers.StaticFileHandler;
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
        var ticketService = new TicketService(ticketRepository);
        var messageDispatcher = new MessageDispatcher();

        //Controllers config
        var loginController = new LoginController(authenticationService);
        var logoutController = new LogoutController(sessionService);
        var registerController = new RegisterController(authenticationService);
        var homeController = new HomeController();
        var pageLinkController = new PageLinksController(sessionService, userService);
        var ticketsController = new TicketsController(sessionService, userService, ticketService);
        var createTicketController = new CreateTicketController(sessionService, userService, ticketService);
        var ticketController = new TicketController(sessionService, userService, ticketService);
        var adminToolsController = new AdminToolsController(sessionService, userService, authenticationService);
        var accountController = new AccountController(sessionService, userService, authenticationService);
        var attachmentController = new AttachmentController(sessionService, userService, ticketService);
        var chatWsController = new ChatWsController(sessionService, userService);
        var chatController = new ChatController(sessionService, userService);

        //Interceptor config
        var sessionInterceptor = new SessionInterceptor<InputStream>(sessionService);
        var authInterceptor = new AuthenticationInterceptor<InputStream>(sessionService);

        //Webserver config
        var webServer = new WebServer(10131);
        Predicate<HttpRequest<InputStream>> skipSession = (r) -> {
            String resource = r.getResource();
            return !resource.equals("/favicon.ico") &&
                    !resource.startsWith("/static");
        };
        Predicate<HttpRequest<InputStream>> skipAuth = (r) -> {
            String resource = r.getResource();
            return skipSession.test(r) &&
                    !resource.equals("/register") &&
                    !resource.equals("/login");
        };
        webServer.registerInterceptor((req, n) ->  {
            long start = System.currentTimeMillis();
            log.info("Requets received {} {}", req.getMethod(), req.getResource());
            HttpResponse<? extends HttpResponse.ResponseBody> res = n.handle(req);
            res.getHeaders().add("Connection", "Keep-Alive");
            log.info("Response status: {}", res.getStatusCode());
            if(log.isDebugEnabled()) {
                log.debug("Response headers: ");
                HttpHeaders headers = res.getHeaders();
                for (Map.Entry<String, List<String>> entry : headers.entries()) {
                    log.debug("{}: {}", entry.getKey(), entry.getValue());
                }
            }
            log.info("Processing time: {}ms", System.currentTimeMillis() - start);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
            return res;
        });
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(sessionInterceptor, skipSession));
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(authInterceptor, skipAuth));
        webServer.registerHttpContext("/static", new StaticFileHandler("/static", "static-files"));
        webServer.registerHttpHandler("/login", loginController);
        webServer.registerHttpHandler("/logout", logoutController);
        webServer.registerHttpHandler("/register", registerController);

        webServer.registerHttpHandler("/", homeController);
        webServer.registerHttpHandler("/page-links", pageLinkController);
        webServer.registerHttpHandler("/tickets", ticketsController);
        webServer.registerHttpHandler("/ticket/create", createTicketController);
        webServer.registerHttpHandler("/ticket", ticketController);
        webServer.registerHttpHandler("/admin-tools", adminToolsController);
        webServer.registerHttpHandler("/account", accountController);
        webServer.registerHttpHandler("/attachment", attachmentController);

        webServer.registerHttpHandler("/chat", chatController);
        webServer.registerWebSocketHandler("/chatroom", chatWsController);

        webServer.start();

    }
}
