package pet.project.model.dto;

public class WeatherDto {
    private Integer id;
    private String currentState;
    private String description;

    public WeatherDto(Integer id, String currentState, String description) {
        this.id = id;
        this.currentState = currentState;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
