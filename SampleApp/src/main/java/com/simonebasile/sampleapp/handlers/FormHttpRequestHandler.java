package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.json.ObjectMapperConfig;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class FormHttpRequestHandler<T> extends MappingRequestHandler<InputStream, T, HttpResponse.ResponseBody> {
    private final Class<T> requestBodyType;
    public FormHttpRequestHandler(Class<T> requestBodyType) {
        this.requestBodyType = requestBodyType;
    }

    @Override
    protected T mapRequestBody(InputStream requestBody) {
        return FormHttpMapper.map(requestBody, requestBodyType);
    }

    @Override
    protected HttpResponse.ResponseBody mapResponseBody(HttpResponse.ResponseBody responseBody) {
        return responseBody;
    }
}
