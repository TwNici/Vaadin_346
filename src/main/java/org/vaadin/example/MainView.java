package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Route("/home")
@PageTitle("Server Dashboard")
@CssImport("./themes/style.css")
public class MainView extends VerticalLayout {

    public MainView() {
        H1 title = new H1("Server Dashboard");
        title.addClassName("title");

        Button start = new Button(new Icon(VaadinIcon.CARET_RIGHT), e -> serverStarter());
        start.addClassName("Start");

        Button stop = new Button(new Icon(VaadinIcon.STOP), e -> serverStopper());
        stop.addClassName("stop");

        Button restart = new Button(new Icon(VaadinIcon.REFRESH), e -> serverRestarter());
        restart.addClassName("restart");

        Button logout = new Button(new Icon(VaadinIcon.EXIT), e -> logout());

        add(title, start, stop, restart, logout);
    }

    private void serverStarter() {
        Notification.show("Server Startet");
        doAction("StartMinecraftServer");
    }

    private void serverStopper() {
        Notification.show("Server Stoppt");
        doAction("StopMinecraftServer");
    }

    private void serverRestarter() {
        Notification.show("Server Restartet");
        doAction("RestartMinecraftServer");
    }

    private void logout() {
        Notification.show("Logout");
    }

    private void doAction(String actionName) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.0.0.4:8080/" + actionName))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Notification.show("Antwort: " + response.body());

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage());
        }
    }
}
