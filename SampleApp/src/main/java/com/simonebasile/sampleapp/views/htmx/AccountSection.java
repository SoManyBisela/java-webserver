package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.SuccessMessage;

import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class AccountSection extends ElementGroup {

    private final InputForm inputForm;

    public AccountSection(User u) {
        inputForm = new InputForm()
                .hxPost("/account")
                .hxTarget("#main")
                .input("oldPassword", "password", l -> l.label().resetContent().text("old password"))
                .input("newPassword", "password", l -> l.label().resetContent().text("new password"))
                .button(a -> a.text("Change password").attr("type", "submit"));
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
