package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.FormButton;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;
import org.w3c.dom.Text;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

/**
 * Represents the login view of the page.
 */
public class LoginView extends View {
    public LoginView(String errorMessage) {
        super();
        HtmlElement msgTarget;
        addContent(
                div().attr("class", "container reglog").content(
                        div().attr("class", "main stack-vertical").content(
                                h(1).text("S").content(span().text("imple ")).text("T").content(span().text("icketing ")).text("S").content(span().text("ystem")),
                                h(2).text("Login"),
                                form()
                                        .attr("action", "/login",
                                                "method", "POST")
                                        .content(
                                                msgTarget = div().attr("class", "stack-vertical").content(
                                                        new TextInputElement("username", "username"),
                                                        new TextInputElement("password", "password").typePassword(),
                                                        new FormButton().content(new MaterialIcon("key_vertical").attr("style", "transform: scaleX(-1) rotate(-45deg)"), span().text("Login"))
                                                                .attr("style", "margin-right: auto")
                                                ),
                                                a().attr("href", "/register").text("Not a user yet? register")
                                        )

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
