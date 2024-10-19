package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.service.AuthenticationService;

public class RegisterHandler extends FormHttpRequestHandler<RegisterRequest> {

    private final AuthenticationService authService;

    public RegisterHandler(AuthenticationService authService) {
        super(RegisterRequest.class);
        this.authService = authService;
    }

    @Override
    protected MappableHttpResponse<HttpResponse.ResponseBody> handleRequest(HttpRequest<RegisterRequest> req) {
        RegisterRequest body = req.getBody();
        HttpHeaders headers = new HttpHeaders();
        if(authService.register(body)) {
            headers.add("Location", "/index.html");
        } else {
            headers.add("Location", "/pub/regfailed.html");
        }
        return new MappableHttpResponse<>(req.getVersion(), 303,
                headers, null);
    }

}
