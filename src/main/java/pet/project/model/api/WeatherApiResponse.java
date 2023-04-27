package pet.project.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    @JsonProperty("weather")
    private List<Weather> weatherList;

    @JsonProperty("main")
    private Main main;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("clouds")
    private Clouds clouds;

    @JsonProperty("dt")
    @JsonDeserialize(using = UnixDateTimestampDeserializer.class)
    private String date;

    @JsonProperty("sys")
    private Sys sys;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("main")
        private String currentState;

        @JsonProperty("description")
        private String description;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        @JsonProperty("temp")
        private Double temperature;

        @JsonProperty("feels_like")
        private Double temperatureFeelsLike;

        @JsonProperty("pressure")
        private Integer pressure;

        @JsonProperty("humidity")
        private Integer humidity;

        @JsonProperty("temp_min")
        private Double temperatureMinimal;

        @JsonProperty("temp_max")
        private Double temperatureMaximum;
    }

    @Getter
    public static class Wind {
        @JsonProperty("speed")
        private Double speed;

        @JsonProperty("deg")
        private Integer deg;

        @JsonProperty("gust")
        private Double gust;
    }

    @Getter
    public static class Clouds {
        @JsonProperty("all")
        private Integer cloudiness;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        @JsonProperty("sunrise")
        @JsonDeserialize(using = UnixTimeTimestampDeserializer.class)
        private String sunriseTime;

        @JsonProperty("sunset")
        @JsonDeserialize(using = UnixTimeTimestampDeserializer.class)
        private String sunsetTime;
    }

    private static class UnixDateTimestampDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            long timestamp = jsonParser.getValueAsLong();
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM HH:mm");
            return formatter.format(date);
        }
    }

    private static class UnixTimeTimestampDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            long timestamp = jsonParser.getValueAsLong();
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            return formatter.format(date);
        }
    }
}
