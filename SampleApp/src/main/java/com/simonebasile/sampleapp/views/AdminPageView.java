package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;
public class AdminPageView extends View {
    public AdminPageView() {
        addContent(
                h(1).text("Admin page"),
                p().text("Work in progress")
        );
    }
}
