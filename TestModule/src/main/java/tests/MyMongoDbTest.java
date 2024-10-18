package tests;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MyMongoDbTest {
    public static void main(String[] args) {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        // Include the following static imports before your class definition
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("sample_pojos").withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Flower> collection = database.getCollection("flowers", Flower.class);

        // Insert three Flower instances
        Flower roseFlower = new Flower("rose", false, 25.4f, Arrays.asList("red", "pink"));
        Flower daisyFlower = new Flower("daisy", true, 21.1f, Arrays.asList("purple", "white"));
        Flower peonyFlower = new Flower("peony", false, 19.2f, Arrays.asList("red", "green"));
        collection.insertMany(Arrays.asList(roseFlower, daisyFlower, peonyFlower));

        // Update a document
        collection.updateOne(
                Filters.lte("height", 22),
                Updates.addToSet("colors", "pink")
        );

        // Delete a document
        collection.deleteOne(Filters.eq("name", "rose"));

        // Return and print all documents in the collection
        List<Flower> flowers = new ArrayList<>();
        collection.find().into(flowers);
        System.out.println(flowers);
    }
}
