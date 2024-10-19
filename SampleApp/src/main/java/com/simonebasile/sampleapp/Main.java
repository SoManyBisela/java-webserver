package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.simonebasile.http.*;
import com.simonebasile.sampleapp.interceptors.AuthenticationInterceptor;
import com.simonebasile.sampleapp.interceptors.InterceptorSkip;
import com.simonebasile.sampleapp.interceptors.SessionInterceptor;
import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.handlers.LoginHandler;
import com.simonebasile.sampleapp.handlers.RegisterHandler;
import com.simonebasile.sampleapp.handlers.StaticFileHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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

        //Handler config
        var loginHandler = new LoginHandler(authenticationService);
        var registerHandler = new RegisterHandler(authenticationService);

        //Interceptor config
        var sessionInterceptor = new SessionInterceptor<InputStream>(sessionService);
        var authInterceptor = new AuthenticationInterceptor<InputStream>(sessionService);


        //Webserver config
        var webServer = new WebServer(10100);
        Predicate<HttpRequest<InputStream>> skipAuthPredicate = (r) -> {
            String resource = r.getResource();
            return !resource.equals("/index.html") &&
                    !resource.equals("/") &&
                    !resource.equals("/register.html") &&
                    !resource.startsWith("/pub") &&
                    !resource.startsWith("/login") &&
                    !resource.startsWith("/register");
        };
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(sessionInterceptor, skipAuthPredicate));
        webServer.registerInterceptor(InterceptorSkip.fromPredicate(authInterceptor, skipAuthPredicate));
        webServer.registerHttpHandler("/login", loginHandler);
        webServer.registerHttpHandler("/register", registerHandler);
        webServer.registerHttpContext("/", new StaticFileHandler("/", "static-files"));
        webServer.registerHttpContext("/api", (req) -> {
            log.info("Entered api context");
            return new HttpResponse<>(req.getVersion(), 404, new HttpHeaders(), null);
        });
        webServer.start();

    }
}
