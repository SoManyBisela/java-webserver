package com.simonebasile.sampleapp.views.html;

import com.simonebasile.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a component of an HTML page. Can be returned as a response body.
 */
public interface IHtmlElement extends HttpResponse.ResponseBody{

    @Override
    default Long contentLength() {
        return null;
    }

    @Override
    default String contentType() {
        return "text/html";
    }

}
