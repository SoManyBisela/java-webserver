package com.simonebasile.sampleapp.views.custom;


import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * Represents a Material Icon in an HTML page.
 */
public class MaterialIcon extends HtmlElement {
    public MaterialIcon(String iconName) {
        super("span");
        text(iconName).attr("class", "material-symbols-outlined");
    }
    public MaterialIcon(String iconName, String addClass) {
        super("span");
        text(iconName).attr("class", "material-symbols-outlined " + addClass);
    }
}
