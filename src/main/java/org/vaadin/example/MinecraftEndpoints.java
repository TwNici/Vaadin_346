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

public class MinecraftEndpoints {

    private final String baseUrl;
    private volatile boolean isStreaming = false;


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
            while (isStreaming) {
                try {
                    URL url = new URL(baseUrl + "/minecraft-console");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String finalLine = line;

                            ui.access(() -> {
                                try {
                                    // Überprüfen, ob der empfangene String JSON ist
                                    if (finalLine.trim().startsWith("{") && finalLine.trim().endsWith("}")) {
                                        // JSON verarbeiten
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode jsonNode = objectMapper.readTree(finalLine);
                                        String timestamp = jsonNode.has("timestamp") ? jsonNode.get("timestamp").asText() : "N/A";
                                        String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "Keine Nachricht";

                                        // Log-Eintrag im UI anzeigen
                                        Div logEntry = new Div();
                                        logEntry.setText("[" + timestamp + "] " + message);
                                        conCanvas.add(logEntry);

                                        // Automatisch scrollen
                                        ui.getPage().executeJs("const el = document.querySelector('.conCanvas'); if (el) { el.scrollTop = el.scrollHeight; }");

                                    } else {
                                        // Nicht-JSON-Daten direkt anzeigen
                                        Div logEntry = new Div();
                                        logEntry.setText("RAW: " + finalLine);
                                        conCanvas.add(logEntry);
                                    }

                                } catch (Exception e) {
                                    Div errorEntry = new Div();
                                    errorEntry.setText("Fehler beim Verarbeiten von JSON: " + e.getMessage());
                                    conCanvas.add(errorEntry);
                                    e.printStackTrace();
                                }
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
            }
        }).start();
    }






}
