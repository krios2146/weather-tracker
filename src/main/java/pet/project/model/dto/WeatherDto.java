package pet.project.model.dto;

import lombok.*;
import pet.project.model.dto.enums.TimeOfDay;
import pet.project.model.dto.enums.WeatherCondition;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherDto {
    private WeatherCondition weatherCondition;

    private TimeOfDay timeOfDay;

    private String description;

    private Double temperature;

    private Double temperatureFeelsLike;

    private Double temperatureMinimum;

    private Double temperatureMaximum;

    private Integer humidity;

    private Integer pressure;

    private Double windSpeed;

    private Integer windDirection;

    private Double windGust;

    private Integer cloudiness;

    private String date;

    private String sunrise;

    private String sunset;
}
