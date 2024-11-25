package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingInterceptorTest {


    private ErrorHandlingInterceptor errorHandlingInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        errorHandlingInterceptor = new ErrorHandlingInterceptor();
    }

    @Test
    void testIntercept_NoException() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/api/resource", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext context = new ApplicationRequestContext();
        final HttpResponse<HttpResponseBody> expectedResponse = new HttpResponse<>(200, new HttpHeaders(), null);
        HttpRequestHandler<InputStream, ApplicationRequestContext> mockNextHandler = (r, c) -> expectedResponse;

        HttpResponse<? extends HttpResponseBody> response = errorHandlingInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(expectedResponse, response);
    }

    @Test
    void testIntercept_ShowableException() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/api/resource", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext context = new ApplicationRequestContext();
        HttpRequestHandler<InputStream, ApplicationRequestContext> mockNextHandler = (r, c) -> { throw new ShowableException(new RuntimeException("Test showable exception")); };
        HttpResponse<? extends HttpResponseBody> response = errorHandlingInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(200, response.getStatusCode());
        assertEquals("none", response.getHeaders().getFirst("HX-Reswap"));
        assertTrue(bodyToString(response.getBody()).contains("Test showable exception"));
    }

    @Test
    void testIntercept_UnexpectedException() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/api/resource", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext context = new ApplicationRequestContext();

        HttpRequestHandler<InputStream, ApplicationRequestContext> mockNextHandler = (r, c) -> { throw new RuntimeException("Test unexpected exception"); };

        HttpResponse<? extends HttpResponseBody> response = errorHandlingInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(200, response.getStatusCode());
        assertEquals("none", response.getHeaders().getFirst("HX-Reswap"));
        assertTrue(bodyToString(response.getBody()).contains("An unexpected error occurred"));
    }

    private String bodyToString(HttpResponseBody body) {
        var baos = new ByteArrayOutputStream();
        try {
            body.write(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }
}