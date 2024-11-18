package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.SuccessMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class AccountSection extends ElementGroup {

    private final HtmlElement inputForm;

    public AccountSection(User u) {
        inputForm = form()
                .hxPost("/account")
                .hxTarget("#main")
                .content(
                        div().attr("class", "stack-vertical").content(
                                new TextInputElement("oldPassword", "old password").typePassword(),
                                new TextInputElement("newPassword", "new password").typePassword(),
                                new TextInputElement("conPassword", "confirm password").typePassword(),
                                button().attr("type", "submit").text("Change password")
                        )
                );
        content.add(h(1).text("Hello " + u.getUsername()));
        content.add(inputForm);
    }

    public AccountSection changePasswordError(String error) {
        inputForm.content(new ErrorMessage(error));
        return this;
    }

    public AccountSection successMessage(String text) {
        inputForm.content(new SuccessMessage(text));
        return this;
    }

}
