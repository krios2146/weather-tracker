package pet.project.model.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("main")
    private String currentState;

    @JsonProperty("description")
    private String description;
}
