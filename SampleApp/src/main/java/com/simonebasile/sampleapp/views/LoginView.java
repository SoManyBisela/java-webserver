package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class LoginView extends View {
    public LoginView(String errorMessage) {
        HtmlElement loginForm = form()
                .attr("action", "/login",
                        "method", "POST"
                )
                .content(
                        table().content(
                                tr().content(
                                        td().content(
                                                label().attr("for", "user")
                                                        .text("username: ")
                                        ),
                                        td().content(
                                                input()
                                                        .attr("type", "text",
                                                                "id", "user",
                                                                "name", "username")
                                        )
                                ),
                                tr().content(
                                        td().content(
                                                label().attr("for", "pw")
                                                        .text("password: ")
                                        ),
                                        td().content(
                                                input()
                                                        .attr("type", "password",
                                                                "id", "pw",
                                                                "name", "password")
                                        )
                                )
                        ),
                        button().attr("type", "submit")
                                .text("Login")
                );
        if(errorMessage != null && !errorMessage.isEmpty()) {
            loginForm.content(new ErrorMessage(errorMessage));
        }
        addContent(
                h(1).text("Simple ticketing system"),
                h(2).text("Login"),
                loginForm,
                a().attr("href", "/register").text("Not a user yet? register")
        );
    }
    public LoginView() {
        this(null);
    }
}
