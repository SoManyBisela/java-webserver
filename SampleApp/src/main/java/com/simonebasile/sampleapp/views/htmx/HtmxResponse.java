package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;

public class HtmxResponse implements HttpResponse.ResponseBody {
    private final IHtmlElement content;

    public HtmxResponse(IHtmlElement content) {
        this.content = content;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        content.write(out);
    }

    @Override
    public Long contentLength() {
        return null;
    }

    @Override
    public String contentType() {
        return "text/html";
    }
}
