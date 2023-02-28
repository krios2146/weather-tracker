package pet.project.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Weather {
    @JsonProperty("main")
    private String currentState;
    @JsonProperty("description")
    private String description;

    public String getCurrentState() {
        return currentState;
    }

    public String getDescription() {
        return description;
    }
}
