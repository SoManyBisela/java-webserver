package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.SelectInputElement;
import com.simonebasile.sampleapp.views.html.custom.SuccessMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class AdminToolsSection extends ElementGroup {

    private final HtmlElement container;

    public AdminToolsSection() {
        content.add(
                form()
                        .hxPost("/admin-tools")
                        .hxTarget("#main")
                        .content(
                                container = div().attr("class", "stack-vertical").content(
                                        h(1).text("Create new user"),
                                        new TextInputElement("username", "username"),
                                        new TextInputElement("password", "password").typePassword(),
                                        new TextInputElement("cpassword", "confirm password").typePassword(),
                                        new SelectInputElement("role", "Role", Arrays.stream(Role.values())
                                                .map(Enum::name)
                                                .map(SelectInputElement.SelectOption::new)
                                                .toArray(SelectInputElement.SelectOption[]::new)),
                                        button().text("Create New User").attr("type", "submit", "class", "default-button")
                                )

                        )
        );
    }

    public AdminToolsSection createUserError(String error) {
        container.content(new ErrorMessage(error));
        return this;
    }

    public AdminToolsSection successMessage(String text) {
        container.content(new SuccessMessage(text));
        return this;
    }

}
