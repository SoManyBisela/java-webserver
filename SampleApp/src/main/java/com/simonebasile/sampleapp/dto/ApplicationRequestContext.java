package com.simonebasile.sampleapp.dto;

import com.simonebasile.http.RequestContext;
import com.simonebasile.sampleapp.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestContext extends RequestContext {
    private String sessionId;
    private User loggedUser;
}
