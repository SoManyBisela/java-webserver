package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class NavbarButtons extends ElementGroup {

    public NavbarButtons(User u) {
        if(u.getRole() == Role.user || u.getRole() == Role.employee) {
            content.add(navButton("Tickets", "/tickets", "#main", "assignment").hxTrigger("click, load"));
        } else if (u.getRole() == Role.admin) {
            content.add(navButton("Admin Tools", "/admin-tools", "#main", "manage_accounts").hxTrigger("click, load"));
        }
        content.add(navButton("Account", "/account", "#main", "person"));
        content.add(navButton("Logout" ,"/logout", "body", "logout").hxConfirm("Are you sure you want to logout?"));
    }

    HtmlElement navButton(String text, String url, String target, String icon) {
        return div().content(
                        span().attr("class", "material-symbols-outlined").text(icon),
                        span().attr("class", "nav-btn-text").text(text)
                ).attr("class", "nav-btn btn")
                .attr("hx-get", url)
                .attr("hx-swap", "inner-html")
                .attr("hx-target", target);
    }
}
