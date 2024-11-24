package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConditionalInterceptorTest {

    @Mock
    private HttpInterceptor<String, String> mockTargetInterceptor;

    @Mock
    private HttpRequestHandler<String, String> mockNextHandler;

    private ConditionalInterceptor<String, String> conditionalInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Predicate<HttpRequest<? extends String>> predicate = request -> request.getResource().contains("intercept");
        conditionalInterceptor = ConditionalInterceptor.fromPredicate(mockTargetInterceptor, predicate);
    }

    @Test
    void testIntercept_ShouldIntercept() {
        HttpRequest<String> request = mock(HttpRequest.class);
        when(request.getResource()).thenReturn("/intercept/resource");
        when(mockTargetInterceptor.intercept(request, "context", mockNextHandler)).thenReturn(new HttpResponse<>(200, new HttpHeaders(), null));

        HttpResponse<? extends ResponseBody> response = conditionalInterceptor.intercept(request, "context", mockNextHandler);

        assertEquals(200, response.getStatusCode());
        verify(mockTargetInterceptor).intercept(request, "context", mockNextHandler);
    }

    @Test
    void testIntercept_ShouldNotIntercept() {
        HttpRequest<String> request = mock(HttpRequest.class);
        when(request.getResource()).thenReturn("/noIntercept/resource");
        when(mockNextHandler.handle(request, "context")).thenReturn(new HttpResponse<>(200, new HttpHeaders(), null));

        HttpResponse<? extends ResponseBody> response = conditionalInterceptor.intercept(request, "context", mockNextHandler);

        assertEquals(200, response.getStatusCode());
        verify(mockTargetInterceptor, never()).intercept(request, "context", mockNextHandler);
        verify(mockNextHandler).handle(request, "context");
    }
}