package com.simonebasile.sampleapp.mapping;

import com.simonebasile.http.format.QueryParameters;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Mapper for form data and query parameters.
 */
@Slf4j
public class FormHttpMapper {

    /**
     * Extracts query params from a resource and maps it to a Java object.
     * @param resource the resource
     * @param type the type
     * @param <T> the type
     * @return the object
     */
    public static <T> T mapHttpResource(String resource , Class<T> type) {
        int qpstart = resource.indexOf("?");
        if(qpstart == -1) {
            qpstart = resource.length() - 1;
        }
        return map(resource.substring(qpstart + 1), type);
    }

    /**
     * Maps query parameters to a Java object.
     * @param queryParams the query parameters
     * @param type the type
     * @param <T> the type
     * @return the object
     */
    public static <T> T map(String queryParams , Class<T> type) {
        final Map<String, String> formInput;
        try {
            log.debug("Params: {}", queryParams);
            formInput = QueryParameters.decode(queryParams);
        } catch (IOException e) {
            log.error("Exception while decoding parameters: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while decoding parameters", e);
        }
        return JsonMapper.mapper.convertValue(formInput, type);
    }

    /**
     * Maps a form input stream to a Java object.
     * @param requestBody the input stream
     * @param type the type
     * @param <T> the type
     * @return the object
     */
    public static <T> T map(InputStream requestBody, Class<T> type) {
        final Map<String, String> formInput;
        try {
            if(log.isDebugEnabled()) {
                byte[] bytes = requestBody.readAllBytes();
                log.debug("Source: {}", new String(bytes, StandardCharsets.UTF_8));
                requestBody = new ByteArrayInputStream(bytes);
            }
            formInput = QueryParameters.decode(requestBody);
            log.debug("Read: {}", formInput);
        } catch (IOException e) {
            log.error("Exception while decoding parameters: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while decoding parameters", e);
        }
        return JsonMapper.mapper.convertValue(formInput, type);
    }

}
