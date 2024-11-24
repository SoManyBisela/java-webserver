package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.FormButton;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

/**
 * Represents the account section of the page.
 * It allows the user to change the password.
 */
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
                                        new FormButton().content(new MaterialIcon("passkey"), span().text("Change password"))
                                )
                        )
                ));
    }
}
