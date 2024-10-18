package com.simonebasile.sampleapp;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.repository.UserRepository;
import com.simonebasile.sampleapp.handlers.LoginHandler;
import com.simonebasile.sampleapp.handlers.RegisterHandler;
import com.simonebasile.sampleapp.model.User;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class SeCuRiTyInTeRcEpToR_XD implements HttpInterceptor<InputStream> {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<InputStream> request, HttpRequestHandler<InputStream> next) {
        log.info("Received request at {}", request.getResource());
        if(!"Miao".equals(request.getHeaders().getFirst("X-Super-Secure-Header"))) {
            return new HttpResponse<>(request.getVersion(), 403, new HttpHeaders(), new ByteResponseBody("Forbidden"));
        }
        final HttpResponse<? extends HttpResponse.ResponseBody> handle = next.handle(request);
        log.info("Service finished for request at {}", request.getResource());
        return handle;
    }
}

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        webapp();
    }

    private static void webapp() {

        //Mongo config
        String dbName = "AssistenzaDB";
        String userCollection = "users";
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<User> usersColl = database.getCollection(userCollection, User.class).withCodecRegistry(pojoCodecRegistry);

        UserRepository userRepository = new UserRepository(usersColl);
        LoginHandler loginHandler = new LoginHandler(userRepository);
        RegisterHandler registerHandler = new RegisterHandler(userRepository);
        WebServer webServer = new WebServer(10100);
        webServer.registerHttpHandler("/login", loginHandler);
        webServer.registerHttpHandler("/register", registerHandler);
        webServer.start();

    }
}
