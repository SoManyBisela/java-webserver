package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.views.custom.FormButton;
import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.sampleapp.views.custom.SelectInputElement;
import com.simonebasile.sampleapp.views.custom.TextInputElement;
import com.simonebasile.web.ssr.component.ElementGroup;

import java.util.Arrays;

import static com.simonebasile.web.ssr.component.HtmlElement.*;

/**
 * Represents the admin tools section of the page.
 * It allows the admin to create new users.
 */
public class AdminToolsSection extends ElementGroup {

    public AdminToolsSection() {
        content.add(
                form()
                        .hxPost("/admin-tools")
                        .hxTarget("#main")
                        .content(
                                div().attr("class", "stack-vertical").content(
                                        h(1).text("Create new user"),
                                        new TextInputElement("username", "username"),
                                        new TextInputElement("password", "password").typePassword(),
                                        new TextInputElement("cpassword", "confirm password").typePassword(),
                                        new SelectInputElement("role", "Role", Arrays.stream(Role.values())
                                                .map(Enum::name)
                                                .map(SelectInputElement.SelectOption::new)
                                                .toArray(SelectInputElement.SelectOption[]::new)),
                                        new FormButton().content(new MaterialIcon("person_add"), span().text("Create New User"))
                                )

                        )
        );
    }
}
