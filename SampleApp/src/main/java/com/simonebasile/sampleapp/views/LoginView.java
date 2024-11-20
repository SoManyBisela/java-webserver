package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;
import org.w3c.dom.Text;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class LoginView extends View {
    public LoginView(String errorMessage) {
        super();
        HtmlElement msgTarget;
        addContent(
                div().attr("class", "container").content(
                        div().attr("class", "main stack-vertical").content(
                                h(1).text("Simple ticketing system"),
                                h(2).text("Login"),
                                form()
                                        .attr("action", "/login",
                                                "method", "POST")
                                        .content(
                                                msgTarget = div().attr("class", "stack-vertical").content(
                                                        new TextInputElement("username", "username"),
                                                        new TextInputElement("password", "password").typePassword(),
                                                        button().attr("type", "submit", "class", "default-button")
                                                                .text("Login")
                                                )
                                        ),
                                a().attr("href", "/register").text("Not a user yet? register")
                        )
                )
        );
        if(errorMessage != null && !errorMessage.isEmpty()) {
            msgTarget.content(new ErrorMessage(errorMessage));
        }
    }
    public LoginView() {
        this(null);
    }
}
