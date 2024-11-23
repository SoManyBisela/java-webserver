package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.model.Role;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A handler that routes the request to a different handler based on the role of the logged user.
 */
public class RoleBasedRouter implements HttpRequestHandler<InputStream, ApplicationRequestContext> {

    private final Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole;

    private RoleBasedRouter(Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole) {
        this.byRole = Map.copyOf(byRole);
    }

    /**
     * Builder for the RoleBasedRouter
     */
    public static class Builder {
        private Map<Role, HttpRequestHandler<InputStream, ApplicationRequestContext>> byRole = new EnumMap<>(Role.class);

        /**
         * Adds a handler for a specific role
         * @param r the role
         * @param handler the handler
         * @return this builder
         */
        public Builder handle(Role r, HttpRequestHandler<InputStream, ApplicationRequestContext> handler) {
            if(byRole.containsKey(r)) {
                throw new IllegalArgumentException("Duplicate role: " + r);
            }
            byRole.put(r, handler);
            return this;
        }

        /**
         * Builds the RoleBasedRouter
         * @return the RoleBasedRouter
         */
        public RoleBasedRouter build() {
            return new RoleBasedRouter(byRole);
        }

    }

    /**
     * Creates a new builder
     * @return the builder
     */
    public static  RoleBasedRouter.Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new RoleBasedRouter for a single handler.
     * Useful for endpoints that are accessible only by a specific role.
     * @param r the role
     * @param handle the handler
     * @return the RoleBasedRouter
     */
    public static RoleBasedRouter of(Role r, HttpRequestHandler<InputStream, ApplicationRequestContext> handle) {
        return new RoleBasedRouter(new HashMap<>(1){{
            put(r, handle);
        }});
    }

    /**
     * Handles the request by routing it to the handler for the role of the logged user.
     * @param r the request
     * @param requestContext the context
     * @return the response
     * @throws ShowableException if the user is not logged, if the user does not have a role or if there is no handler for the role of the user
     */
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends InputStream> r, ApplicationRequestContext requestContext) {
        if(requestContext.getLoggedUser() == null || requestContext.getLoggedUser().getRole() == null) {
            throw new ShowableException("You cannot access this page");
        }
        var handler = byRole.get(requestContext.getLoggedUser().getRole());
        if(handler == null) {
            throw new ShowableException("You cannot access this page");
        }
        return handler.handle(r, requestContext);
    }
}
