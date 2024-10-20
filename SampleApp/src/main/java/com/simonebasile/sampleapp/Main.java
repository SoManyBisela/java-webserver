package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.simonebasile.http.*;
import com.simonebasile.sampleapp.controllers.HomeController;
import com.simonebasile.sampleapp.controllers.LoginController;
import com.simonebasile.sampleapp.controllers.LogoutController;
import com.simonebasile.sampleapp.controllers.RegisterController;
import com.simonebasile.sampleapp.interceptors.AuthenticationInterceptor;
import com.simonebasile.sampleapp.interceptors.InterceptorSkip;
import com.simonebasile.sampleapp.interceptors.SessionInterceptor;
import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.handlers.StaticFileHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        webapp();
    }

    private static void webapp() {

        //Mongo config
        var dbName = "AssistenzaDB";
        var userCollection = "users";
        var sessionCollection = "sessions";
        var pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));
        var mongoClient = MongoClients.create();
        var database = mongoClient.getDatabase(dbName);
        var usersColl = database.getCollection(userCollection, User.class).withCodecRegistry(pojoCodecRegistry);
        var sessionColl = database.getCollection(sessionCollection, SessionData.class).withCodecRegistry(pojoCodecRegistry);

        //Repository config
        var userRepository = new UserRepository(usersColl);
        var sessionRepository = new SessionRepository(sessionColl);

        //Services config
        var sessionService = new SessionService(sessionRepository);
        var authenticationService = new AuthenticationService(userRepository, sessionService);
        var userService = new UserService(userRepository);

        //Controllers config
        var loginController = new LoginController(authenticationService);
        var logoutController = new LogoutController(sessionService);
        var registerController = new RegisterController(authenticationService);
        var homeController = new HomeController(sessionService, userService);


        //Interceptor config
        var sessionInterceptor = new SessionInterceptor<InputStream>(sessionService);
        var authInterceptor = new AuthenticationInterceptor<InputStream>(sessionService);


        //Webserver config
        var webServer = new WebServer(10100);
        Predicate<HttpRequest<InputStream>> skipSession = (r) -> {
            String resource = r.getResource();
            return !resource.equals("/favicon.ico") &&
                    !resource.startsWith("/pub");
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
            return res;
        });
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(sessionInterceptor, skipSession));
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(authInterceptor, skipAuth));
        webServer.registerHttpHandler("/login", loginController);
        webServer.registerHttpHandler("/logout", logoutController);
        webServer.registerHttpHandler("/register", registerController);
        webServer.registerHttpHandler("/", homeController);
        webServer.registerHttpContext("/", new StaticFileHandler("/", "static-files"));
        webServer.start();

    }
}
