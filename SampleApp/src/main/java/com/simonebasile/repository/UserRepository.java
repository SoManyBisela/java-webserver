package com.simonebasile.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.simonebasile.sampleapp.model.User;
import org.bson.BsonValue;

import java.util.Optional;

public record UserRepository(MongoCollection<User> collection) {
    public Optional<User> getUser(String username) {
        return Optional.ofNullable(collection.find(Filters.eq("username", username)).first());
    }

    public void insert(User u) {
        InsertOneResult insertOneResult = collection.insertOne(u);
        BsonValue insertedId = insertOneResult.getInsertedId();
        if(insertedId != null) {
            u.setId(insertedId.asObjectId().getValue());
        }
    }
}
