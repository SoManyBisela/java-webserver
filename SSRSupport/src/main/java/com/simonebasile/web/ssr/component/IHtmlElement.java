package com.simonebasile.web.ssr.component;

import com.simonebasile.http.response.HttpResponseBody;

/**
 * Represents a component of an HTML page. Can be returned as a response body.
 */
public interface IHtmlElement extends HttpResponseBody {

    @Override
    default Long contentLength() {
        return null;
    }

    @Override
    default String contentType() {
        return "text/html";
    }

}
