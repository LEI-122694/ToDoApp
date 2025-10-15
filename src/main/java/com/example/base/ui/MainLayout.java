package com.example.base.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
public final class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createHeader(), new Scroller(createSideNav()));
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("App");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        var spacer = new Div();
        spacer.addClassNames("flex-grow-1"); // versão simples sem constantes

        // Botão PDF
        Button pdfBtn = new Button("PDF", new Icon(VaadinIcon.DOWNLOAD_ALT));
        pdfBtn.addClickListener(e -> {
            // Abre o endpoint numa nova aba; o browser faz o download
            UI.getCurrent().getPage().open("/api/exports/tasks.pdf");
        });

        var header = new Div(appLogo, appName, spacer, pdfBtn);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER, Width.FULL);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
