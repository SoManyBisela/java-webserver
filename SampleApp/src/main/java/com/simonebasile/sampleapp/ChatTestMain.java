package com.simonebasile.sampleapp;

import com.simonebasile.http.format.QueryParameters;
import com.simonebasile.http.handlers.StaticFileHandler;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.server.WebServer;
import com.simonebasile.sampleapp.controller.ChatWsController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.MainView;

import java.io.IOException;
import java.util.Map;

/**
 * Secondary main class to run a webserver that only exposes the chat. Used to test the chat without db access
 * */
public class ChatTestMain {

    //Main class
    public static void main(String[] args) {
        var ws = WebServer.builder().requestContextFactory(ApplicationRequestContext::new).build();
        ws.registerInterceptor((r, c, n) -> {
            String user;
            String role;
            String sid = r.getHeaders().getCookie("sid");
            if(sid != null && !r.getResource().contains("logout")) {
                var spl = sid.split("#");
                user = spl[0];
                role = spl[1];
            } else {
                String resource = r.getResource();
                int i = resource.indexOf("?");
                if(i == -1) {
                    return new HttpResponse<>(401, new ByteResponseBody("Unauthorized"));
                }
                String param = resource.substring(i + 1);
                try {
                    Map<String, String> decode = QueryParameters.decode(param);
                    user = decode.get("user");
                    role = decode.get("role");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            c.setLoggedUser(new User(user, "", Role.valueOf(role)));
            var res = n.handle(r, c);
            res.getHeaders().setCookie("sid", user + "#" + role);
            return res;
        });
        ws.registerHttpHandler("/", (r, c) -> new HttpResponse<>(new MainView(c.getLoggedUser())));
        ws.registerHttpContext("/static", new StaticFileHandler<>("static-files"));
        ws.registerWebSocketContext("/chat", new ChatWsController());
        ws.start();
    }
}
