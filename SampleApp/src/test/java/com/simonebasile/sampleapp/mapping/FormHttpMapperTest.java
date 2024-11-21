package com.simonebasile.sampleapp.mapping;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class FormHttpMapperTest {

    @Test
    void testMapHttpResource() {
        String resource = "/api/resource?param1=value1&param2=value2";
        TestType result = FormHttpMapper.mapHttpResource(resource, TestType.class);

        assertNotNull(result);
        assertEquals("value1", result.getParam1());
        assertEquals("value2", result.getParam2());
    }

    @Test
    void testMapQueryParams() {
        String queryParams = "param1=value1&param2=value2";
        TestType result = FormHttpMapper.map(queryParams, TestType.class);

        assertNotNull(result);
        assertEquals("value1", result.getParam1());
        assertEquals("value2", result.getParam2());
    }

    @Test
    void testMapInputStream() throws Exception {
        String queryParams = "param1=value1&param2=value2";
        InputStream inputStream = new ByteArrayInputStream(queryParams.getBytes());

        TestType result = FormHttpMapper.map(inputStream, TestType.class);

        assertNotNull(result);
        assertEquals("value1", result.getParam1());
        assertEquals("value2", result.getParam2());
    }

    @Test
    void testMapInputStreamWithExceptionAfterSecondByte() {
        InputStream inputStream = new InputStream() {
            private int bytesRead = 0;

            @Override
            public int read() throws IOException {
                if (bytesRead >= 2) {
                    throw new IOException("Test exception after second byte");
                }
                bytesRead++;
                return 'a';
            }
        };

        RuntimeException exception = assertThrows(RuntimeException.class, () -> FormHttpMapper.map(inputStream, TestType.class));
        assertEquals("An error occurred while decoding parameters", exception.getMessage());
    }

    @Test
    void testMapHttpResource_NoQueryParams() {
        String resource = "/api/resource";
        TestType result = FormHttpMapper.mapHttpResource(resource, TestType.class);

        assertNotNull(result);
        assertNull(result.getParam1());
        assertNull(result.getParam2());
    }

    @Setter
    @Getter
    static class TestType {
        private String param1;
        private String param2;

    }
}