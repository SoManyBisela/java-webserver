package com.simonebasile.sampleapp.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.simonebasile.sampleapp.model.User;
import org.bson.BsonValue;

import java.util.Objects;
import java.util.Optional;

/**
 * Repository for managing users.
 */
public final class UserRepository {
    private final MongoCollection<User> collection;

    public UserRepository(MongoCollection<User> collection) {
        this.collection = collection;
    }

    /**
     * Gets a user by username.
     * @param username the username
     * @return the user
     */
    public User getUser(String username) {
        return collection.find(Filters.eq("username", username)).first();
    }

    /**
     * Creates s user.
     * @param u the user
     */
    public void insert(User u) {
        InsertOneResult insertOneResult = collection.insertOne(u);
        BsonValue insertedId = insertOneResult.getInsertedId();
        if (insertedId != null) {
            u.setId(insertedId.asObjectId().getValue());
        }
    }

    /**
     * Updates a user.
     * @param u the user
     */
    public void updateUser(User u) {
        collection.replaceOne(Filters.eq("username", u.getUsername()), u);
    }
}
