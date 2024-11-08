package com.simonebasile.sampleapp.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonMapper {
    public static final ObjectMapper mapper = buildObjectMapper();

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    public static String toString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonMappingFailure(e);
        }
    }

    public static <T> T parse(byte[] source, Class<T> type) {
        try {
            return mapper.readValue(source, type);
        } catch (IOException e) {
            throw new JsonMappingFailure(e);
        }
    }

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
