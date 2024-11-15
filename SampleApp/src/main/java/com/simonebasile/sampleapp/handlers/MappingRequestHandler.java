package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.*;


public abstract class MappingRequestHandler<T, R, U> implements HttpRequestHandler<T> {
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends T> r) {
        MappableHttpResponse<? extends U> httpResponse = handleRequest(new HttpRequest<>(r, mapRequestBody(r.getBody())));
        return new HttpResponse<>(httpResponse, httpResponse.statusCode, mapResponseBody(httpResponse.getBody()));
    }

    protected abstract MappableHttpResponse<? extends U> handleRequest(HttpRequest<R> request);
    protected abstract R mapRequestBody(T requestBody);
    protected abstract HttpResponse.ResponseBody mapResponseBody(U responseBody);

    protected static class MappableHttpResponse<T> extends HttpMessage<T> {
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
