package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MinecraftEndpoints {

    private final String baseUrl;
    private final Timer timer = new Timer(true);  // Ein globaler Timer für den gesamten Stream
    private final List<String> logEntries = new ArrayList<>();

    public MinecraftEndpoints(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String doAction(String actionName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + actionName))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("API-Fehler: " + response.statusCode() + " - " + response.body());
        }
    }

    public void clearLog() {
        logEntries.clear();
        System.out.println("Log-Einträge wurden gelöscht.");
    }

    public String sendCommand(String command) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String jsonPayload = String.format("{\"command\": \"%s\"}", command);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/send-command"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return "Befehl erfolgreich gesendet: " + command;
        } else {
            throw new IOException("API-Fehler: " + response.statusCode() + " - " + response.body());
        }
    }

    public void startLogStream(UI ui, Div conCanvas) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + "/minecraft-console"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    ui.access(() -> {
                        conCanvas.removeAll();
                        conCanvas.setText(response.body());
                        conCanvas.getElement().executeJs("this.scrollTop = this.scrollHeight;");
                    });

                } catch (Exception e) {
                    ui.access(() -> conCanvas.setText("Fehler beim Abrufen der Logs: " + e.getMessage()));
                    e.printStackTrace();
                }
            }
        }, 0, 3000); // Aktualisierung alle 3 Sekunden
    }

    public void stopLogStream() {
        timer.cancel();
    }
}
