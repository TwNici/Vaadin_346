package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.example.jwt.JwtUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

@Route("/home")
@PageTitle("Server Dashboard")
@CssImport("./themes/style.css")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private final MinecraftEndpoints minecraftEndpoints;
    private Div conCanvas;
    private TextField sendCommand;

    public MainView() {
        this.minecraftEndpoints = new MinecraftEndpoints("http://10.0.1.4:5000");

        H1 title = new H1("Server Dashboard");
        title.addClassName("title");

        Button start = new Button(new Icon(VaadinIcon.CARET_RIGHT), e -> serverStarter());
        Button stop = new Button(new Icon(VaadinIcon.STOP), e -> serverStopper());
        Button restart = new Button(new Icon(VaadinIcon.REFRESH), e -> serverRestarter());
        Button logout = new Button(new Icon(VaadinIcon.EXIT), e -> logout());

        sendCommand = new TextField();
        Button sendCommandButton = new Button(new Icon(VaadinIcon.PLAY), e -> sendCommands());

        conCanvas = new Div();
        conCanvas.addClassName("conCanvas");

        add(title, start, stop, restart, logout, conCanvas, sendCommand, sendCommandButton);

        minecraftEndpoints.startLogStream(UI.getCurrent(), conCanvas);
    }

    private void serverStarter() {
        try {
            Notification.show(minecraftEndpoints.doAction("start-minecraft"));
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void serverStopper() {
        try {
            Notification.show(minecraftEndpoints.doAction("stop-minecraft"));
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void serverRestarter() {
        try {
            Notification.show(minecraftEndpoints.doAction("restart-minecraft"));
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void sendCommands() {
        String command = sendCommand.getValue();
        if (command == null || command.isEmpty()) {
            Notification.show("Bitte einen Befehl eingeben!");
            return;
        }

        try {
            Notification.show(minecraftEndpoints.sendCommand(command));
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void logout() {
        Notification.show("Logout");
        VaadinSession.getCurrent().setAttribute("authToken", null);
        UI.getCurrent().navigate("");
    }

    private void handleError(Exception e) {
        Notification.show("Fehler: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = (String) VaadinSession.getCurrent().getAttribute("authToken");
        if (token == null || JwtUtil.validateToken(token) == null) {
            event.rerouteTo("");
        }
    }
}

