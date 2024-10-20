package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;

import static com.simonebasile.sampleapp.views.html.HtmlElement.h;
import static com.simonebasile.sampleapp.views.html.HtmlElement.p;

public class EmployeePageView extends View {
    public EmployeePageView() {
        addContent(
                h(1).text("Employee page"),
                p().text("Work in progress")
        );
    }
}
