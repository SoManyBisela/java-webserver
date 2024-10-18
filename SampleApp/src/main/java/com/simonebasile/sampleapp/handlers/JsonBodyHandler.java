package com.simonebasile.sampleapp.handlers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.simonebasile.http.*;
import com.simonebasile.sampleapp.exceptions.BadRequestException;
import com.simonebasile.sampleapp.json.ObjectMapperConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public abstract class JsonBodyHandler<T> implements HttpRequestHandler<InputStream> {

    private final Class<T> bodyType;

    public JsonBodyHandler(Class<T> bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<InputStream> r) {
        MappableHttpResponse<?> httpResponse = handleRequest(new HttpRequest<>(r, mapRequestBody(r.getBody())));

        return new HttpResponse<>(httpResponse, httpResponse.statusCode, mapResponseBody(httpResponse.getBody()));
    }

    private HttpResponse.ResponseBody mapResponseBody(Object body) {
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

    public abstract MappableHttpResponse<?> handleRequest(HttpRequest<T> req);

    public static class MappableHttpResponse<T> extends HttpMessage<T> {
        private final int statusCode;

        public MappableHttpResponse(HttpVersion version, int statusCode, HttpHeaders headers, T body) {
            super(version, headers, body);
            this.statusCode = statusCode;
        }

        public MappableHttpResponse(MappableHttpResponse<?> source, T body) {
            super(source, body);
            this.statusCode = source.statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
