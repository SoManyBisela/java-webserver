package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.base.View;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class HomeView extends View {
    public HomeView(User user) {
        addContent(
                h(1).text("Welcome " + user.getUsername()),
                h(2).text("You are a " + user.getRole()),
                a().attr("href", "/tickets").text("Go to your tickets"),
                form().attr("action", "/logout", "method", "POST")
                        .content(button().attr("type", "submit" ).text("Logout"))
        );
    }
}
