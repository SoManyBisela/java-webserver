package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class RegisterView extends View {
    public RegisterView(String errorMessage) {
        super();
        HtmlElement msgTarget;
        addContent(
                div().attr("class", "container").content(
                        div().attr("class", "main stack-vertical").content(
                                h(1).text("Simple ticketing system"),
                                h(2).text("Register"),
                                form()
                                        .attr("action", "/register",
                                                "method", "POST"
                                        )
                                        .content(
                                                msgTarget = div().attr("class", "stack-vertical").content(
                                                        new TextInputElement("username", "username"),
                                                        new TextInputElement("password", "password").typePassword(),
                                                        new TextInputElement("cpassword", "confirm password").typePassword(),
                                                        button().attr("type", "submit", "class", "default-button")
                                                                .text("Register")
                                                )
                                        ),
                                a().attr("href", "/login").text("Already registered? login")
                        )
                )
        );
        if (errorMessage != null && !errorMessage.isEmpty()) {
            msgTarget.content(new ErrorMessage(errorMessage));
        }
    }

    public RegisterView() {
        this(null);
    }
}
