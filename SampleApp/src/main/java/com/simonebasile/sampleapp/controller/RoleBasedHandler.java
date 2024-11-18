package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class RoleBasedHandler implements HttpRequestHandler<InputStream, ApplicationRequestContext> {

    private final Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole;

    private RoleBasedHandler(Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole) {
        this.byRole = Map.copyOf(byRole);
    }

    public static class Builder {
        private Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole = new EnumMap<>(Role.class);

        public Builder handle(Role r, HttpRequestHandler<InputStream, ApplicationRequestContext> handler) {
            if(byRole.containsKey(r)) {
                throw new IllegalArgumentException("Duplicate role: " + r);
            }
            byRole.put(r, handler);
            return this;
        }

        public RoleBasedHandler build() {
            return new RoleBasedHandler(byRole);
        }

    }

    public static  RoleBasedHandler.Builder builder() {
        return new Builder();
    }

    public static  RoleBasedHandler of(Role r, HttpRequestHandler<InputStream, ApplicationRequestContext> handle) {
        return new RoleBasedHandler(new HashMap<>(1){{
            put(r, handle);
        }});
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends InputStream> r, ApplicationRequestContext requestContext) {
        if(requestContext.getLoggedUser() == null || requestContext.getLoggedUser().getRole() == null) {
            return ResponseUtils.redirect(r, "/");
        }
        var handler = byRole.get(requestContext.getLoggedUser().getRole());
        if(handler == null) {
            return ResponseUtils.redirect(r, "/");
        }
        return handler.handle(r, requestContext);
    }
}
