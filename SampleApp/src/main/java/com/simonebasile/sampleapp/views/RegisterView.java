package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class RegisterView extends View {
    public RegisterView(String errorMessage) {
        super(false);
        HtmlElement loginForm = form()
                .attr("action", "/register",
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
                                .text("Register")
                );
        if(errorMessage != null && !errorMessage.isEmpty()) {
            loginForm.content(new ErrorMessage(errorMessage));
        }
        addContent(
                h(1).text("Simple ticketing system"),
                h(2).text("Register"),
                loginForm,
                a().attr("href", "/login").text("Already registered? login")
        );
    }
    public RegisterView() {
        this(null);
    }
}
