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

    private MessageList conCanvas;
    private Div conNotiA;
    private Console console;
    private Timer timer;
    private TextField sendCommand;

    public MainView() {
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

        sendCommand = new TextField();
        sendCommand.addClassName("sendCommand");

        Button sendCommandButton = new Button(new Icon(VaadinIcon.PLAY), e -> sendCommands());
        sendCommandButton.addClassName("sendCommandButton");

        console = new Console();
        conCanvas = new MessageList();
        conCanvas.setId("consoleOutput");
        conCanvas.addClassName("conCanvas");

        conNotiA = new Div("Status wird geladen...");
        conNotiA.addClassName("conNotiA");

        add(title, start, stop, restart, logout, conCanvas, conNotiA, sendCommand, sendCommandButton);

        startLogStream();
        startServerStatusUpdater();
    }


    private void startServerStatusUpdater() {
        UI ui = UI.getCurrent();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ui != null && ui.getSession() != null && ui.getSession().getSession() != null) {
                    ui.access(() -> {
                        console.fetchServerStatus();
                        conNotiA.setText(console.getNoti());
                    });
                }
            }
        }, 0, 500);
    }


    private void stopServerStatusUpdater() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
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
        doAction("restart-minecraft");
    }

    private void logout() {
        Notification.show("Logout");
        VaadinSession.getCurrent().setAttribute("authToken", null);
        stopServerStatusUpdater();
        UI.getCurrent().navigate("");
    }

    private void startLogStream() {
        getElement().executeJs(
                "const eventSource = new EventSource('http://10.0.1.4:5000/minecraft-console');" +
                        "eventSource.onmessage = (event) => {" +
                        "   const conCanvas = document.querySelector('.conCanvas');" +
                        "   if (conCanvas) {" +
                        "       conCanvas.textContent += event.data + '\\n';" +
                        "   }" +
                        "};"
        );
    }

    private void sendCommands() {
        String command = sendCommand.getValue();
        if (command == null || command.isEmpty()) {
            Notification.show("Bitte einen Befehl eingeben!");
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonPayload = String.format("{\"command\": \"%s\"}", command);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.0.1.4:5000/send-command"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Notification.show("Befehl erfolgreich gesendet: " + command);
            } else {
                Notification.show("Fehler: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            Notification.show("Verbindungsfehler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void doAction(String actionName) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://10.0.1.4:5000/" + actionName))
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
        if (token == null || JwtUtil.validateToken(token) == null) {
            stopServerStatusUpdater();
            event.rerouteTo("");
        }
    }

    private String getTokenFromSession() {
        return (String) VaadinSession.getCurrent().getAttribute("authToken");
    }
}
