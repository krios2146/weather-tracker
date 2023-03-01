package pet.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Location;
import pet.project.model.api.Weather;
import pet.project.model.api.WeatherApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApiService {
    private static final String APP_ID = "ff54fce37c4721c1b5e9e22bbd8e9274";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final HttpClient client = HttpClient.newHttpClient();

    public Weather getWeatherForLocation(Location location) throws IOException, InterruptedException {
        HttpRequest request = buildRequest(location);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        WeatherApiResponse weatherApiResponse = objectMapper.readValue(response.body(), WeatherApiResponse.class);
        return weatherApiResponse.getWeather();
    }

    private static HttpRequest buildRequest(Location location) {
        URI uri = buildUri(location);
        return HttpRequest.newBuilder(uri)
                .GET()
                .build();
    }

    private static URI buildUri(Location location) {
        return URI.create(API_URL
                + "?lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude()
                + "&appid=" + APP_ID
                + "&units=" + "metric");
    }
}
