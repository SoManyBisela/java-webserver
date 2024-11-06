package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.InputForm;
import lombok.Builder;

import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class HomeView extends View {
    public record Errors(String createUserError) {
        public Errors() {
            this(null);
        }
    }

    public HomeView(User user) {
        this(user, new Errors());
    }
    public HomeView(User user, Errors errors) {
        addContent(
                h(1).text("Welcome " + user.getUsername()),
                h(2).text("You are a " + user.getRole()),
                userSection(user, errors),
                form().attr("action", "/logout", "method", "POST")
                        .content(button().attr("type", "submit" ).text("Logout"))
        );
    }

    private HtmlElement userSection(User user, Errors errors) {
        HtmlElement userContent = div();
        if(user.getRole() == Role.admin) {
        } else if(user.getRole() == Role.user){
            userContent.content(
                    a().attr("href", "/tickets").text("Go to your tickets")
            );
        } else if(user.getRole() == Role.employee) {
            userContent.content(
                    a().attr("href", "/tickets").text("Go to tickets")
            );
        }
        return userContent;
    }
}
