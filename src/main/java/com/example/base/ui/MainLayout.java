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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.example.qrcode.QrCodeGenerator;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;

@Layout
public final class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        // Mantém header e side nav; adiciona botão QR no drawer
        addToDrawer(createHeader(), new Scroller(createSideNav()), createQrButton());
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
        //nav.addItem(new SideNavItem("Task Deadline Chart", "charts"));
        nav.addItem(new SideNavItem("📊 Task Chart", "upcoming-tasks-chart"));
        // Add Currency Exchange Calculator tab manually
        nav.addItem(new SideNavItem("Currency Exchange Calculator", "currency-exchange", new Icon(VaadinIcon.MONEY)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    /**
     * Botão no drawer que abre um diálogo centralizado com a imagem do QR.
     * Usa QrCodeGenerator.generate(...) para obter um BufferedImage e converte para data URL base64.
     */
    private Div createQrButton() {
        Icon qrIcon = VaadinIcon.QRCODE.create();
        qrIcon.addClassNames(IconSize.MEDIUM); // opcional

        Button qrBtn = new Button("QR Code");
        qrBtn.setIcon(qrIcon);
        qrBtn.addClassNames(Margin.Horizontal.MEDIUM, Padding.Vertical.SMALL);

        qrBtn.addClickListener(event -> {
            // Texto codificado no QR (altera conforme precisares)
            String textToEncode = "https://www.youtube.com/";

            try {
                // Gera o BufferedImage usando a tua classe
                BufferedImage qrImage = QrCodeGenerator.generate(textToEncode, 320, 320);

                // Converte BufferedImage para PNG bytes
                byte[] pngBytes;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(qrImage, "PNG", baos);
                    baos.flush();
                    pngBytes = baos.toByteArray();
                }

                // Converte para Base64 e cria data URL (sem StreamResource)
                String base64 = Base64.getEncoder().encodeToString(pngBytes);
                String dataUrl = "data:image/png;base64," + base64;

                // Cria a Image com o data URL
                Image img = new Image(dataUrl, "QR Code");
                img.setAlt("QR Code");
                img.setWidth("320px");
                img.setHeight("320px");
                img.getStyle().set("display", "block");

                // Wrapper centrado para a imagem no dialog
                Div wrapper = new Div();
                wrapper.getStyle().set("display", "flex");
                wrapper.getStyle().set("flex-direction", "column");
                wrapper.getStyle().set("align-items", "center");
                wrapper.getStyle().set("justify-content", "center");
                wrapper.getStyle().set("padding", "var(--lumo-space-m)");
                wrapper.getStyle().set("gap", "var(--lumo-space-s)");

                H4 title = new H4("QR Code");
                title.getStyle().set("margin", "0");

                NativeButton close = new NativeButton("Fechar");
                close.addClassName("vaadin-button");

                wrapper.add(title, img, close);

                Dialog dialog = new Dialog(wrapper);
                dialog.setCloseOnOutsideClick(true);
                dialog.setCloseOnEsc(true);

                close.addClickListener(ev -> dialog.close());

                // Estética — tamanho máximo responsivo
                dialog.getElement().getStyle().set("max-width", "90vw");
                dialog.getElement().getStyle().set("padding", "0");
                dialog.open();

            } catch (Exception ex) {
                Notification.show("Erro ao gerar QR: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });

        Div container = new Div(qrBtn);
        container.addClassNames(Margin.Horizontal.MEDIUM);
        return container;
    }
}
