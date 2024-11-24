package com.simonebasile.http;

import com.simonebasile.http.response.ResponseBody;

/**
 * Represents an HTTP response.
 *
 * @param <T> the type of the body of the HTTP response
 */
public class HttpResponse<T extends ResponseBody> extends HttpMessage<T>{
    protected final int statusCode;

    /**
     * Creates a new HTTP response with status code 200.
     *
     * @param body the body of the response
     */
    public HttpResponse(T body) {
        this(200, new HttpHeaders(), body);
    }

    /**
     * Creates a new HTTP response with the given status code.
     *
     * @param statusCode the status code of the response
     * @param body the body of the response
     */
    public HttpResponse(int statusCode, T body) {
        this(statusCode, new HttpHeaders(), body);
    }

    /**
     * Creates a new HTTP response with the given status code and headers.
     *
     * @param statusCode the status code of the response
     * @param headers the headers of the response
     * @param body the body of the response
     */
    public HttpResponse(int statusCode, HttpHeaders headers, T body) {
        super(headers, body);
        this.statusCode = statusCode;
    }

    /**
     * Creates copy of the given response with the given body.
     *
     * @param source the response to copy
     * @param body the body of the response
     */
    public HttpResponse(HttpResponse<?> source, T body) {
        this(source, source.statusCode, body);
    }

    /**
     * Creates a copy of the given response with the given status code and body.
     *
     * @param source the response to copy
     * @param statusCode the status code of the response
     * @param body the body of the response
     */
    public HttpResponse(HttpMessage<?> source, int statusCode, T body) {
        super(source, body);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
