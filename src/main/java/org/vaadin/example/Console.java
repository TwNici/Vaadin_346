package org.vaadin.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Console {


    public String getNoti() {
        return fetchServerStatus();
    }

    public String fetchServerStatus() {
        String noti;
        try {
            URL url = new URL("http://10.0.1.4:5000/status-minecraft");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            int responseCode = con.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String serverStatus = jsonResponse.optString("serverStatus", "Error");

                switch (serverStatus) {
                    case "Running":
                        noti = "Status: 🟢";
                        break;
                    case "Stopped":
                        noti = "Status: 🔴";
                        break;
                    case "Starting":
                        noti = "Status: 🟡";
                        break;
                    default:
                        noti = "Status: 🟠";
                }
            } else {
                noti = "❌";
            }
        } catch (IOException | JSONException e) {
            noti = "Status: ❌";
        }
        return noti;
    }
}
