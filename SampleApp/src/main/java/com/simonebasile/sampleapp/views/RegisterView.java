package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class RegisterView extends View {
    public RegisterView(String errorMessage) {
        super(false);
        HtmlElement loginForm = form()
                .attr("action", "/register",
                        "method", "POST"
                )
                .content(
                        div().attr("class", "stack-vertical").content(
                                new TextInputElement("username", "username"),
                                new TextInputElement("password", "password").typePassword(),
                                new TextInputElement("cpassword", "confirm password").typePassword(),
                                button().attr("type", "submit", "class", "default-button")
                                        .text("Register")
                        )
                );
        if (errorMessage != null && !errorMessage.isEmpty()) {
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
