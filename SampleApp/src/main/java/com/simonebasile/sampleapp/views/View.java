package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class View extends BaseView{
    public View() {
        addHead(new HtmlElement("title").text("Ticketing"));
        addCss("pub/common.css");
        addJsScript("pub/common.js");
    }
}
