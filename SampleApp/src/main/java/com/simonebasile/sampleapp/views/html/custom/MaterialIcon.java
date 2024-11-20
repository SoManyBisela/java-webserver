package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class MaterialIcon extends HtmlElement {
    public MaterialIcon(String iconName) {
        super("span");
        text(iconName).attr("class", "material-symbols-outlined");
    }
}
