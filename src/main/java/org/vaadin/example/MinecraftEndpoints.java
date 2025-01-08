package org.vaadin.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final List<String> logEntries;
    private final Timer timer;

    public MinecraftEndpoints(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logEntries = new ArrayList<>();
        this.timer = new Timer(true);
    }

    public void clearLog() {
        logEntries.clear();
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
                    String finalLine = response.body();

                    ui.access(() -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(finalLine);
                            String timestamp = jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : "N/A";
                            String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "Keine Nachricht";

                            Div logEntry = new Div();
                            logEntry.setText("[" + timestamp + "] " + message);
                            conCanvas.add(logEntry);
                            conCanvas.getElement().executeJs("this.scrollTop = this.scrollHeight;");

                        } catch (Exception e) {
                            Div errorEntry = new Div();
                            errorEntry.setText("Fehler beim Verarbeiten von JSON: " + e.getMessage());
                            conCanvas.add(errorEntry);
                        }
                    });
                } catch (Exception e) {
                    ui.access(() -> {
                        Div errorEntry = new Div();
                        errorEntry.setText("Fehler beim Abrufen der Logs: " + e.getMessage());
                        conCanvas.add(errorEntry);
                    });
                }
            }
        }, 0, 3000); // Alle 3 Sekunden
    }

}
