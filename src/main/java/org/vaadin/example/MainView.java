package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.JsonValue;
import org.vaadin.example.jwt.JwtUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Route("/home")
@PageTitle("Server Dashboard")
@CssImport("./themes/style.css")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    public MainView() throws IOException {
        H1 title = new H1("Server Dashboard");
        title.addClassName("title");

        H3 conText = new H3("Console");
        conText.addClassName("conText");

        Button start = new Button(new Icon(VaadinIcon.CARET_RIGHT), e -> serverStarter());
        start.addClassName("start");

        Button stop = new Button(new Icon(VaadinIcon.STOP), e -> serverStopper());
        stop.addClassName("stop");

        Button restart = new Button(new Icon(VaadinIcon.REFRESH), e -> serverRestarter());
        restart.addClassName("restart");

        Button logout = new Button(new Icon(VaadinIcon.EXIT), e -> logout());
        logout.addClassName("logout");

        console console = new console();

        console.consoleConnectionTest();
        String conNoti = console.getNoti();
        Div conCanvas = new Div("Console stuff");
        conCanvas.addClassName("conCanvas");
        Div conNotiA = new Div(conNoti);
        conNotiA.addClassName("conNotiA");

        add(title, start, stop, restart, logout, conCanvas, conNotiA);
    }

    private void serverStarter() {
        Notification.show("Server Startet");
        doAction("start-minecraft");
    }

    private void serverStopper() {
        Notification.show("Server Stoppt");
        doAction("stop-minecraft");
    }

    private void serverRestarter() {
        Notification.show("Server Restartet");
        doAction("RestartMinecraftServer");
    }

    private void logout() {
        Notification.show("Logout");
        VaadinSession.getCurrent().setAttribute("authToken", null);
        UI.getCurrent().navigate("");
    }

    private void doAction(String actionName) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Sicherstellen, dass der korrekte Endpunkt angesprochen wird
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://51.107.13.118:5000/" + actionName))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Notification.show("Aktion erfolgreich: " + response.body());
            } else {
                Notification.show("API-Fehler: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            Notification.show("Verbindungsfehler: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = getTokenFromSession();
        System.out.println(token);

        if (token == null || JwtUtil.validateToken(token) == null) {
            event.rerouteTo("");
        }
    }

    private String getTokenFromSession() {
        return (String) VaadinSession.getCurrent().getAttribute("authToken");
    }
}
