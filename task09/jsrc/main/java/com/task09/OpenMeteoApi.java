package com.task09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenMeteoApi {
    private static final String url = "https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30" +
            ".5238&timezone=Europe/Kyiv&hourly=temperature_2m";

    public static String getWeatherForecast() {

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
