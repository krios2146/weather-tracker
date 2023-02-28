package pet.project.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeatherApiResponse {
    @JsonProperty("weather")
    private Weather weather;
    @JsonProperty("temp")
    private Double temperature;
    @JsonProperty("feels_like")
    private Double temperatureFeelsLike;
    @JsonProperty("pressure")
    private Integer pressure;
    @JsonProperty("humidity")
    private Integer humidity;
    @JsonProperty("wind")
    private Wind wind;
    @JsonProperty("clouds")
    private Clouds clouds;

    public Weather getWeather() {
        return weather;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getTemperatureFeelsLike() {
        return temperatureFeelsLike;
    }

    public Integer getPressure() {
        return pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    private static class Wind {
        @JsonProperty("speed")
        private Double speed;
        @JsonProperty("deg")
        private Integer deg;
        @JsonProperty("gust")
        private Double gust;

        public Double getSpeed() {
            return speed;
        }

        public Integer getDeg() {
            return deg;
        }

        public Double getGust() {
            return gust;
        }
    }

    private static class Clouds {
        @JsonProperty("all")
        private Integer cloudiness;

        public Integer getCloudiness() {
            return cloudiness;
        }
    }
}
