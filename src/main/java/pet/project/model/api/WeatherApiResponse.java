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
    private List<WeatherApiModel> weatherList;

    @JsonProperty("temp")
    private Double temperature;

    @JsonProperty("feels_like")
    private Double temperatureFeelsLike;

    @JsonProperty("temp_min")
    private Double temperatureMinimal;

    @JsonProperty("temp_max")
    private Double temperatureMaximum;

    @JsonProperty("pressure")
    private Integer pressure;

    @JsonProperty("humidity")
    private Integer humidity;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("clouds")
    private Clouds clouds;

    @JsonProperty("dt")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    private String date;
    
    @JsonProperty("sunrise")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    private String sunriseTime;

    @JsonProperty("sunset")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    private String sunsetTime;

    @Getter
    private static class Wind {
        @JsonProperty("speed")
        private Double speed;

        @JsonProperty("deg")
        private Integer deg;

        @JsonProperty("gust")
        private Double gust;
    }

    @Getter
    private static class Clouds {
        @JsonProperty("all")
        private Integer cloudiness;
    }

    private static class UnixTimestampDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            long timestamp = jsonParser.getValueAsLong();
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return formatter.format(date);
        }
    }
}
