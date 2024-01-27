package com.task09;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.fasterxml.jackson.annotation.JsonProperty;

@DynamoDBTable(tableName = "Weather")
public class WeatherForecast {
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    private String id;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    private Forecast forecast;

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "forecast")
    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    public static class Forecast {

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        private double elevation;
        @JsonProperty(value = "generationtime_ms")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        private double generationTimeMs;


        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
        private Hourly hourly;

        @JsonProperty(value = "hourly_units")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
        private HourlyUnits hourlyUnits;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        private double latitude;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        private double longitude;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        private String timezone;

        @JsonProperty(value = "timezone_abbreviation")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        private String timezoneAbbreviation;

        @JsonProperty(value = "utc_offset_seconds")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        private long utcOffsetSeconds;

        @DynamoDBAttribute(attributeName = "elevation")
        public double getElevation() {
            return elevation;
        }

        public void setElevation(double elevation) {
            this.elevation = elevation;
        }

        @DynamoDBAttribute(attributeName = "generationtime_ms")
        public double getGenerationTimeMs() {
            return generationTimeMs;
        }

        public void setGenerationTimeMs(double generationTimeMs) {
            this.generationTimeMs = generationTimeMs;
        }

        @DynamoDBAttribute(attributeName = "hourly")
        public Hourly getHourly() {
            return hourly;
        }

        public void setHourly(Hourly hourly) {
            this.hourly = hourly;
        }

        @DynamoDBAttribute(attributeName = "hourly_units")
        public HourlyUnits getHourlyUnits() {
            return hourlyUnits;
        }

        public void setHourlyUnits(HourlyUnits hourlyUnits) {
            this.hourlyUnits = hourlyUnits;
        }

        @DynamoDBAttribute(attributeName = "latitude")
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @DynamoDBAttribute(attributeName = "longitude")
        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        @DynamoDBAttribute(attributeName = "timezone")
        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        @DynamoDBAttribute(attributeName = "timezone_abbreviation")
        public String getTimezoneAbbreviation() {
            return timezoneAbbreviation;
        }

        public void setTimezoneAbbreviation(String timezoneAbbreviation) {
            this.timezoneAbbreviation = timezoneAbbreviation;
        }

        @DynamoDBAttribute(attributeName = "utc_offset_seconds")
        public long getUtcOffsetSeconds() {
            return utcOffsetSeconds;
        }

        public void setUtcOffsetSeconds(long utcOffsetSeconds) {
            this.utcOffsetSeconds = utcOffsetSeconds;
        }
    }

    public static class Hourly {
        @JsonProperty(value = "temperature_2m")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.L)
        private List<Double> temperature2m;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.L)
        private List<String> time;

        @DynamoDBAttribute(attributeName = "temperature_2m")
        public List<Double> getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(List<Double> temperature2m) {
            this.temperature2m = temperature2m;
        }

        @DynamoDBAttribute(attributeName = "time")
        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }
    }

    public static class HourlyUnits {

        @JsonProperty(value = "temperature_2m")

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        private String temperature2m;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        private String time;

        @DynamoDBAttribute(attributeName = "temperature_2m")
        public String getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(String temperature2m) {
            this.temperature2m = temperature2m;
        }

        @DynamoDBAttribute(attributeName = "time")
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}

