package com.simonebasile.sampleapp.mapping;

import com.simonebasile.http.format.QueryParameters;
import com.simonebasile.sampleapp.json.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormHttpMapper {
    private static final Logger log = LoggerFactory.getLogger(FormHttpMapper.class);

    public static <T> T mapHttpResource(String resource , Class<T> type) {
        int qpstart = resource.indexOf("?");
        if(qpstart == -1) {
            qpstart = resource.length();
        }
        return map(resource.substring(qpstart), type);
    }

    public static <T> T map(String queryParams , Class<T> type) {
        final Map<String, String> formInput;
        try {
            log.debug("Params: {}", queryParams);
            formInput = QueryParameters.decode(queryParams);
        } catch (IOException e) {
            log.error("Exception while decoding parameters: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while decoding parameters", e);
        }
        return ObjectMapperConfig.jsonMapper.convertValue(formInput, type);
    }

    public static <T> T map(InputStream requestBody, Class<T> type) {
        final Map<String, String> formInput;
        try {
            if(log.isDebugEnabled()) {
                byte[] bytes = requestBody.readAllBytes();
                log.debug("Body: {}", new String(bytes, StandardCharsets.UTF_8));
                requestBody = new ByteArrayInputStream(bytes);
            }
            formInput = QueryParameters.decode(requestBody);
        } catch (IOException e) {
            log.error("Exception while decoding parameters: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while decoding parameters", e);
        }
        return ObjectMapperConfig.jsonMapper.convertValue(formInput, type);
    }

}
