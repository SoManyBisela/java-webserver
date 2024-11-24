package com.simonebasile.sampleapp.views.html;

import com.simonebasile.http.response.ResponseBody;

/**
 * Represents a component of an HTML page. Can be returned as a response body.
 */
public interface IHtmlElement extends ResponseBody {

    @Override
    default Long contentLength() {
        return null;
    }

    @Override
    default String contentType() {
        return "text/html";
    }

}
