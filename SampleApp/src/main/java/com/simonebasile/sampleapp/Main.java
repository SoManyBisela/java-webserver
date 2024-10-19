package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.repository.SessionRepository;
import com.simonebasile.repository.UserRepository;
import com.simonebasile.sampleapp.dto.SessionData;
import com.simonebasile.sampleapp.handlers.LoginHandler;
import com.simonebasile.sampleapp.handlers.RegisterHandler;
import com.simonebasile.sampleapp.handlers.StaticFileHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        webapp();
    }

    private static void webapp() {

        //Mongo config
        String dbName = "AssistenzaDB";
        String userCollection = "users";
        String sessionCollection = "sessions";
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<User> usersColl = database.getCollection(userCollection, User.class).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<SessionData> sessionColl = database.getCollection(sessionCollection, SessionData.class).withCodecRegistry(pojoCodecRegistry);

        //Repository config
        UserRepository userRepository = new UserRepository(usersColl);
        SessionRepository sessionRepository = new SessionRepository(sessionColl);

        //Services config
        SessionService sessionService = new SessionService(sessionRepository);
        AuthenticationService authenticationService = new AuthenticationService(userRepository, sessionService);

        //Handler config
        LoginHandler loginHandler = new LoginHandler(authenticationService);
        RegisterHandler registerHandler = new RegisterHandler(authenticationService);


        //Webserver config
        WebServer webServer = new WebServer(10100);
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
