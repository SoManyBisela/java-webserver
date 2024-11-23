package com.simonebasile.sampleapp.views.html;

import com.simonebasile.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a component of an HTML page. Can be returned as a response body.
 */
public abstract class IHtmlElement implements HttpResponse.ResponseBody{

    @Override
    public Long contentLength() {
        return null;
    }

    @Override
    public String contentType() {
        return "text/html";
    }

}
