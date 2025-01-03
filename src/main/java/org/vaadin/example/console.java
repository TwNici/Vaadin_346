package org.vaadin.example;

import com.vaadin.flow.component.notification.Notification;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class console {
    private String noti;
    public void consoleConnectionTest() {
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                noti = "Verbindung konnte aufgebaut werden";
            }

        } catch (IOException e) {
            noti = "Verbindung fehlgeschlagen";
        }
    }

    public String getNoti() {
        return noti;
    }

}
