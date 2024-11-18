package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.SuccessMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class AccountSection extends ElementGroup {

    private final HtmlElement container;

    public AccountSection(User u) {
        content.add(form()
                .hxPost("/account")
                .hxTarget("#main")
                .content(
                        container = div().attr("class", "stack-vertical").content(
                                h(1).text("Change password"),
                                div().attr("class", "stack-vertical").content(
                                        new TextInputElement("oldPassword", "old password").typePassword(),
                                        new TextInputElement("newPassword", "new password").typePassword(),
                                        new TextInputElement("conPassword", "confirm password").typePassword(),
                                        button().attr("class", "default-button", "type", "submit").text("Change password")
                                )
                        )
                ));
    }

    public AccountSection changePasswordError(String error) {
        container.content(new ErrorMessage(error));
        return this;
    }

    public AccountSection successMessage(String text) {
        container.content(new SuccessMessage(text));
        return this;
    }

}
