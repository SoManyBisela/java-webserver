package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.SuccessMessage;

import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.div;
import static com.simonebasile.sampleapp.views.html.HtmlElement.input;

public class AdminToolsSection extends ElementGroup {

    private final InputForm inputForm;

    public AdminToolsSection() {
        inputForm = new InputForm()
                .hxPost("/admin-tools")
                .hxTarget("#main")
                .input("username", "text")
                .input("password", "password")
                .select("role", Arrays.stream(Role.values())
                        .map(Enum::name)
                        .map(InputForm.SelectOption::new)
                        .toArray(InputForm.SelectOption[]::new))
                .button(a -> a.text("Create New User").attr("type", "submit"));
        content.add(inputForm);
    }

    public AdminToolsSection createUserError(String error) {
        inputForm.content(new ErrorMessage(error));
        return this;
    }

    public AdminToolsSection successMessage(String text) {
        inputForm.content(new SuccessMessage(text));
        return this;
    }

}
