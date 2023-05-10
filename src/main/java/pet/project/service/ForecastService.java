package pet.project.service;

import pet.project.model.api.ForecastApiResponse;
import pet.project.model.api.ForecastApiResponse.HourlyForecast;
import pet.project.model.api.entity.Weather;
import pet.project.model.dto.WeatherDto;
import pet.project.model.dto.enums.TimeOfDay;
import pet.project.model.dto.enums.WeatherCondition;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForecastService {
    public List<WeatherDto> getHourlyForecast(ForecastApiResponse response) {
        return response.getForecasts().stream()
                .map(ForecastService::buildWeatherDto)
                .collect(Collectors.toList());
    }

    public List<WeatherDto> getDailyForecast(ForecastApiResponse response) {
        List<HourlyForecast> hourlyForecasts = response.getForecasts();

        Map<LocalDate, List<HourlyForecast>> dailyForecasts = getDailyForecasts(hourlyForecasts);

        return dailyForecasts.values()
                .stream()
                .map(ForecastService::buildWeatherDto)
                .collect(Collectors.toList());
    }

    private static Map<LocalDate, List<HourlyForecast>> getDailyForecasts(List<HourlyForecast> hourlyForecasts) {
        Map<LocalDate, List<HourlyForecast>> dailyForecasts = new HashMap<>();

        LocalDate currentDay = LocalDate.from(hourlyForecasts.get(0).getDate());
        LocalDate lastDay = LocalDate.from(hourlyForecasts.get(hourlyForecasts.size() - 1).getDate());

        while (currentDay.isBefore(lastDay)) {
            dailyForecasts.put(currentDay, getForecastsForDay(hourlyForecasts, currentDay));
            currentDay = currentDay.plusDays(1);
        }

        return dailyForecasts;
    }

    private static WeatherDto buildWeatherDto(HourlyForecast hourlyForecast) {
        Weather weather = hourlyForecast.getWeathers().get(0);
        return WeatherDto.builder()
                .date(hourlyForecast.getDate())
                .description(weather.getDescription())
                .temperature(hourlyForecast.getMain().getTemperature())
                .timeOfDay(TimeOfDay.getTimeOfDayForTime(hourlyForecast.getDate()))
                .weatherCondition(WeatherCondition.getWeatherConditionForCode(weather.getId()))
                .build();
    }

    private static WeatherDto buildWeatherDto(List<HourlyForecast> hourlyForecasts) {
        return WeatherDto.builder()
                .weatherCondition(getAverageWeather(hourlyForecasts))
                .temperature(getAverageTemperature(hourlyForecasts))
                .temperatureMaximum(getMaxTemperature(hourlyForecasts))
                .temperatureMinimum(getMinTemperature(hourlyForecasts))
                .date(hourlyForecasts.get(0).getDate())
                .build();
    }

    private static List<HourlyForecast> getForecastsForDay(List<HourlyForecast> forecasts, LocalDate day) {
        return forecasts.stream()
                .filter(forecast -> forecast.getDate().toLocalDate().isEqual(day))
                .collect(Collectors.toList());
    }

    private static Double getAverageTemperature(List<HourlyForecast> forecasts) {
        return forecasts.stream()
                .map(forecast -> forecast.getMain().getTemperature())
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
    }

    private static Double getMinTemperature(List<HourlyForecast> forecasts) {
        return forecasts.stream()
                .map(forecast -> forecast.getMain().getTemperature())
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }

    private static Double getMaxTemperature(List<HourlyForecast> forecasts) {
        return forecasts.stream()
                .map(forecast -> forecast.getMain().getTemperature())
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    private static WeatherCondition getAverageWeather(List<HourlyForecast> forecasts) {
        return forecasts.stream()
                .map(forecast -> forecast.getWeathers().get(0).getId())
                .map(WeatherCondition::getWeatherConditionForCode)
                .collect(Collectors.groupingBy(
                        weatherCondition -> weatherCondition,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(WeatherCondition.UNDEFINED);
    }
}
