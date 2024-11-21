package com.simonebasile.http;

import java.io.InputStream;

/**
 * This interface is used to read the body of a request.
 * It can be overridden by application developers to configure how the body of a request is read by the web server.
 */
public interface BodyReader<T> {
    T readBody(InputStream inputStream, int length);
}
