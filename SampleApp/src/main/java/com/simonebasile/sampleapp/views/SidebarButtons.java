package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class SidebarButtons extends ElementGroup {

    public SidebarButtons(User u) {
        if(u.getRole() == Role.user || u.getRole() == Role.employee) {
            content.add(navButton("Tickets", "/tickets", "#main").hxTrigger("click, load"));
        } else if (u.getRole() == Role.admin) {
            content.add(navButton("Admin Tools", "/admin-tools", "#main" ).hxTrigger("click, load"));
        }
        content.add(navButton("Account", "/account", "#main"));
        content.add(navButton("Logout" ,"/logout", "body").hxConfirm("Are you sure you want to logout?"));
    }

    HtmlElement navButton(String text, String url, String target) {
        //TODO add icons to buttons and remove this method
        return navButton(text, url, target, "/static/icon/placeholder.png");
    }

    HtmlElement navButton(String text, String url, String target, String icon) {
        return div().content(
                        div().attr("class", "nav-btn-ico", "style", "background-image: url(" + icon + ")"),
                        span().attr("class", "nav-btn-text").text(text)
                ).attr("class", "nav-btn btn")
                .attr("hx-get", url)
                .attr("hx-swap", "inner-html")
                .attr("hx-target", target);
    }
}
