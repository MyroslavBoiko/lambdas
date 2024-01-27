package com.task09;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenMeteoXRayApi {
    private static final String url = "https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30" +
            ".5238&timezone=Europe/Kyiv&hourly=temperature_2m";

    public static WeatherForecast.Forecast getWeatherForecast() {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            ObjectMapper mapper = new ObjectMapper();
            WeatherForecast.Forecast forecast = mapper.readValue(inputStream, WeatherForecast.Forecast.class);
            EntityUtils.consume(entity);
            return forecast;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
