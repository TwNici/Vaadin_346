package org.vaadin.example;

import com.vaadin.flow.component.notification.Notification;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class console {
    private String noti;
    public void consoleConnectionTest() {
        try {
            URL url = new URL("http://51.107.13.118:5000/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                noti = "Status: \uD83D\uDFE2";
            }

        } catch (IOException e) {
            noti = "Status: \uD83D\uDD34";
        }
    }

    public String getNoti() {
        return noti;
    }

}
