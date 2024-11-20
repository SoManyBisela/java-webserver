package com.simonebasile.sampleapp;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

public class Utils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static IHtmlElement oobAdd(String targetId, IHtmlElement content) {
        return HtmlElement.div().attr("id", targetId).hxSwapOob("beforeend").content(content);
    }
}
