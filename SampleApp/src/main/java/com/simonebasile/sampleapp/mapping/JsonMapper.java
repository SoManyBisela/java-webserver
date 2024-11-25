package com.simonebasile.sampleapp.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Utility class for JSON serialization and deserialization.
 */
public class JsonMapper {
    public static final ObjectMapper mapper = buildObjectMapper();

    /**
     * Configures the object mapper.
     * @return the object mapper
     */
    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    /**
     * Serializes an object to a JSON string.
     * @param object the object
     * @return the JSON string
     */
    public static String toString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonMappingFailure(e);
        }
    }

    /**
     * Deserializes a JSON byte array to an object.
     * @param source the JSON byte array
     * @param type the type
     * @param <T> the type
     * @return the object
     */
    public static <T> T parse(byte[] source, Class<T> type) {
        try {
            return mapper.readValue(source, type);
        } catch (IOException e) {
            throw new JsonMappingFailure(e);
        }
    }

    /**
     * Deserializes a JSON string to an object.
     * @param source the JSON string
     * @param type the type
     * @param <T> the type
     * @return the object
     */
    public static <T> T parse(String source, Class<T> type) {
        try {
            return mapper.readValue(source, type);
        } catch (JsonProcessingException e) {
            throw new JsonMappingFailure(e);
        }
    }

    private static class JsonMappingFailure extends RuntimeException {
        public JsonMappingFailure(Exception e) {
            super(e);
        }
    }
}
