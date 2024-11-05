package com.simonebasile.sampleapp;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;

public class ResponseUtils {
    public static HttpResponse<HttpResponse.ResponseBody> redirect(HttpRequest<?> req, String location) {
        final String hxReq = req.getHeaders().getFirst("Hx-Request");
        if(hxReq != null && hxReq.equals("true")) {
            //send hx redirect
            HttpHeaders headers = new HttpHeaders();
            headers.add("Hx-Redirect", location);
            return new HttpResponse<>(req.getVersion(), 200, headers, null);
        } else {
            //send http redirect
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", location);
            return new HttpResponse<>(req.getVersion(), 303, headers, null);
        }
    }
}