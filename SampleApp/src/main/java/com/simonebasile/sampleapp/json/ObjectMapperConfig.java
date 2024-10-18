package com.simonebasile.sampleapp.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperConfig {
    public static final ObjectMapper jsonMapper = buildObjectMapper();

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
