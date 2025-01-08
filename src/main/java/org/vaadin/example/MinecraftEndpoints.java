package org.vaadin.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MinecraftEndpoints {

    private final String baseUrl;
    private volatile boolean isStreaming = false;
    private final List<String> logEntries;

    public MinecraftEndpoints(String baseUrl) {
        this.baseUrl = baseUrl;
        this.logEntries = new ArrayList<>();
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
        isStreaming = true;
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/minecraft-console");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while (isStreaming && (line = reader.readLine()) != null) {
                        String finalLine = line;
                        logEntries.add(finalLine);

                        ui.access(() -> {
                            conCanvas.removeAll();
                            logEntries.forEach(log -> {
                                try {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    JsonNode jsonNode = objectMapper.readTree(log);
                                    String timestamp = jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : "N/A";
                                    String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "Keine Nachricht";
                                    Div logEntry = new Div();
                                    logEntry.setText("[" + timestamp + "] " + message);
                                    conCanvas.add(logEntry);
                                } catch (Exception e) {
                                    Div errorEntry = new Div();
                                    errorEntry.setText("RAW: " + log);
                                    conCanvas.add(errorEntry);
                                }
                            });
                            ui.getPage().executeJs("const el = document.querySelector('.conCanvas'); if (el) { el.scrollTop = el.scrollHeight; }");
                        });
                    }
                }
            } catch (Exception e) {
                ui.access(() -> {
                    Div errorEntry = new Div();
                    errorEntry.setText("Fehler beim Log-Stream: " + e.getMessage());
                    conCanvas.add(errorEntry);
                });
                e.printStackTrace();
            }
        }).start();
    }

}
