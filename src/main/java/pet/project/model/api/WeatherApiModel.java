package pet.project.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiModel {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("main")
    private String currentState;
    @JsonProperty("description")
    private String description;

    public Integer getId() {
        return id;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getDescription() {
        return description;
    }
}
