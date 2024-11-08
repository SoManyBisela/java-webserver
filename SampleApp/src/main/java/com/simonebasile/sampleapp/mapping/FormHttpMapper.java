package com.simonebasile.sampleapp.mapping;

import com.simonebasile.http.format.QueryParameters;
import com.simonebasile.sampleapp.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class FormHttpMapper {

    public static <T> T mapHttpResource(String resource , Class<T> type) {
        int qpstart = resource.indexOf("?");
        if(qpstart == -1) {
            qpstart = resource.length() - 1;
        }
        return map(resource.substring(qpstart + 1), type);
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
        return JsonMapper.mapper.convertValue(formInput, type);
    }

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
