package com.simonebasile.sampleapp.controllers;

import com.simonebasile.sampleapp.views.base.View;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UnauthorizedPage extends View {
    public UnauthorizedPage() {
        url("/unauthorized");
        addContent(
                h(1).text("Unauthorized Access"),
                p().text("You tried accessing an unauthorized page")
        );
    }
}
