package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;
import com.simonebasile.sampleapp.views.html.custom.SelectInputElement;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

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
                                        button().content(new MaterialIcon("person_add"), span().text("Create New User"))
                                                .attr("type", "submit", "class", "default-button")
                                )

                        )
        );
    }
}
