package com.simonebasile.http;

import java.io.InputStream;

public interface BodyReader<T> {
    T readBody(InputStream inputStream, int length);
}
