package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.User;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class HomeView extends View{
    public HomeView(User user) {
        addContent(
                h(1).text("Welcome " + user.getUsername()),
                h(2).text("You are a " + user.getRole()),
                form().attr("action", "/logout", "method", "POST")
                        .content(button().attr("type", "submit" ).text("Logout"))
        );
    }
}
