package pet.project.model.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Main {
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
