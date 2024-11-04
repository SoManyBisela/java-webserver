package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.util.ArrayList;
import java.util.List;

public class SidebarButtons extends HtmxResponse {

    public SidebarButtons(User u) {
        super(create(u));
    }

    private static IHtmlElement create(User u) {
        final List<NavButton> links = new ArrayList<>();
        if(u.getRole() == Role.user || u.getRole() == Role.employee) {
            links.add(new NavButton("Tickets", "/tickets", "#main"));
        } else if (u.getRole() == Role.admin) {
            links.add(new NavButton("Admin Tools", "/admin-tools", "#main"));
        }
        links.add(new NavButton("Account", "/account", "#main"));

        return new ElementGroup(links);
    }
}
