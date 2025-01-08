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
        ui.getPage().executeJs(
                "const eventSource = new EventSource('http://10.0.1.4:5000/minecraft-console');" +
                        "eventSource.onmessage = (event) => {" +
                        "   const conCanvas = document.querySelector('.conCanvas');" +
                        "   if (conCanvas) {" +
                        "       try {" +
                        "           const logData = JSON.parse(event.data);" + // JSON parsen
                        "           const timestamp = logData.timestamp || 'N/A';" +
                        "           const message = logData.message || 'Keine Nachricht';" +
                        "           const logEntry = `[${timestamp}] ${message}\\n`;" +
                        "           conCanvas.textContent += logEntry;" +
                        "           conCanvas.scrollTop = conCanvas.scrollHeight;" + // Automatisches Scrollen
                        "       } catch (e) {" +
                        "           console.error('Fehler beim Verarbeiten von JSON:', e);" +
                        "           conCanvas.textContent += 'Fehler beim Verarbeiten von JSON: ' + event.data + '\\n';" +
                        "       }" +
                        "   }" +
                        "};"
        );
    }






}
