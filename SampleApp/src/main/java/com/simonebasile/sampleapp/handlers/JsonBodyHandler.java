package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.*;
import com.simonebasile.sampleapp.exceptions.BadRequestException;
import com.simonebasile.sampleapp.json.ObjectMapperConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public abstract class JsonBodyHandler<T> extends MappingRequestHandler<InputStream, T, Object>{

    private final Class<T> bodyType;

    public JsonBodyHandler(Class<T> bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    protected T mapRequestBody(InputStream in) {
        try {
            T parsed = ObjectMapperConfig.jsonMapper.readValue(in, bodyType);
            if(in.read() != -1) {
                throw new BadRequestException("Invalid body: body does not end with the json");
            }
            return parsed;
        } catch (IOException e) {
            throw new BadRequestException("Error parsing body", e);
        }
    }

    @Override
    protected HttpResponse.ResponseBody mapResponseBody(Object body) {
        return new JsonResponseBody(body);
    }

    private static class JsonResponseBody implements HttpResponse.ResponseBody {
        private final Object source;
        public JsonResponseBody(Object source) {
            this.source = source;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            ObjectMapperConfig.jsonMapper.writeValue(out, source);
        }

        @Override
        public Long contentLength() {
            return null;
        }

        @Override
        public String contentType() {
            return "application/json";
        }
    }

}
