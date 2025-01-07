package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MinecraftEndpoints {

    private final String baseUrl;

    public MinecraftEndpoints(String baseUrl) {
        this.baseUrl = "51.107.13.118";
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
                "const eventSource = new EventSource('" + baseUrl + "/minecraft-console');" +
                        "eventSource.onmessage = (event) => {" +
                        "   const conCanvas = document.querySelector('.conCanvas');" +
                        "   if (conCanvas) {" +
                        "       conCanvas.textContent += event.data + '\\n';" +
                        "   }" +
                        "};"
        );
    }
}