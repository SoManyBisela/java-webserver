package com.simonebasile.sampleapp;

import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;

public class Utils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static HttpResponse<HttpResponseBody> redirect(HttpRequest<?> req, String location) {
        final String hxReq = req.getHeaders().getFirst("Hx-Request");
        if(hxReq != null && hxReq.equals("true")) {
            //send hx redirect
            HttpHeaders headers = new HttpHeaders();
            headers.add("Hx-Redirect", location);
            return new HttpResponse<>(200, headers, null);
        } else {
            //send http redirect
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", location);
            return new HttpResponse<>(303, headers, null);
        }
    }
}
