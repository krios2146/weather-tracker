package pet.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Location;
import pet.project.model.api.ApiLocation;
import pet.project.model.api.Weather;
import pet.project.model.api.WeatherApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class WeatherApiService {
    private static final String APP_ID = "ff54fce37c4721c1b5e9e22bbd8e9274";
    private static final String BASE_API_URL = "https://api.openweathermap.org";
    private static final String WEATHER_API_URL_SUFFIX = "/data/2.5/weather";
    private static final String GEOCODING_API_URL_SUFFIX = "/geo/1.0/direct";

    private final HttpClient client = HttpClient.newHttpClient();

    public Weather getWeatherForLocation(Location location) throws IOException, InterruptedException {
        URI uri = buildUriForWeatherRequest(location);
        HttpRequest request = buildRequest(uri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        WeatherApiResponse weatherApiResponse = objectMapper.readValue(response.body(), WeatherApiResponse.class);
        return weatherApiResponse.getWeather();
    }

    public List<ApiLocation> getLocationsByName(String nameOfLocation) throws IOException, InterruptedException {
        URI uri = buildUriForGeocodingRequest(nameOfLocation);
        HttpRequest request = buildRequest(uri);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        List<ApiLocation> apiLocationList = objectMapper.readValue(
                response.body(),
                new TypeReference<List<ApiLocation>>() {
                });
        return apiLocationList;
    }

    private static HttpRequest buildRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .GET()
                .build();
    }

    private static URI buildUriForWeatherRequest(Location location) {
        return URI.create(BASE_API_URL + WEATHER_API_URL_SUFFIX
                + "?lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude()
                + "&appid=" + APP_ID
                + "&units=" + "metric");
    }

    private static URI buildUriForGeocodingRequest(String nameOfLocation) {
        // Somehow without explicit limit api returns only 1 object
        return URI.create(BASE_API_URL + GEOCODING_API_URL_SUFFIX
                + "?q=" + nameOfLocation
                + "&limit=5"
                + "&appid=" + APP_ID);
    }
}
