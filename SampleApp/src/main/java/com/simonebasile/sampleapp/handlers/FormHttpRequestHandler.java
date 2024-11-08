package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;

import java.io.*;

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
