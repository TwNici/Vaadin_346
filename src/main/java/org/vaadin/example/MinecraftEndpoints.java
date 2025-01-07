package org.vaadin.example;

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
            try {
                URL url = new URL(baseUrl + "/minecraft-console");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while (isStreaming && (line = reader.readLine()) != null) {
                        String finalLine = line;

                        ui.access(() -> {
                            Div logEntry = new Div();
                            logEntry.setText(finalLine);
                            conCanvas.add(logEntry);

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
