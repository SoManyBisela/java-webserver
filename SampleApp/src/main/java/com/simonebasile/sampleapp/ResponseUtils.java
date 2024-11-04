package com.simonebasile.sampleapp;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.views.base.BaseView;

public class ResponseUtils {
    //Redirects to get method
    public static HttpResponse<HttpResponse.ResponseBody> redirect(HttpVersion version, String location) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", location);
        return new HttpResponse<>(version, 303, headers, null);
    }
}
