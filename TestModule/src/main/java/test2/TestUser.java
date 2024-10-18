package test2;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;


public class TestUser {

    public static void main(String[] args) {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                ));

        execFuncioning("test_user_crud", "users", pojoCodecRegistry);
        execFuncioning("test_user_crud", "test_error", null);
        execFuncioning("test_user_crud", "test_error", pojoCodecRegistry);
    }

    private static void execFuncioning(String _db, String _collection, CodecRegistry pojoCodecRegistry) {
        System.out.println("Testing " + _db + "." + _collection + (pojoCodecRegistry == null ? "without registry" : "with registry"));
        try (MongoClient mongoClient = MongoClients.create()) {
            var db = mongoClient.getDatabase(_db);
            if(pojoCodecRegistry != null) {
                db = db.withCodecRegistry(pojoCodecRegistry);
            }
            var collection = db.getCollection(_collection, User.class);
            collection.insertOne(new User("simone", "pazz", "admin"));
            var users = new ArrayList<User>();
            collection.find().into(users);
            System.out.print("\t");
            System.out.println(users);
        }
        System.out.println("Test end");
    }
}
