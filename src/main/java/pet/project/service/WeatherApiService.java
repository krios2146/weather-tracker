package pet.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.exception.api.GeocodingApiCallException;
import pet.project.exception.api.WeatherApiCallException;
import pet.project.model.Location;
import pet.project.model.api.LocationApiResponse;
import pet.project.model.api.WeatherApiResponse;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherApiResponse getWeatherForLocation(Location location) throws WeatherApiCallException {
        try {
            URI uri = buildUriForWeatherRequest(location);
            HttpRequest request = buildRequest(uri);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(response.body(), WeatherApiResponse.class);

        } catch (Exception e) {
            throw new WeatherApiCallException("Issues with calling api for location with id = " + location.getId());
        }
    }

    public List<LocationApiResponse> getLocationsByName(String nameOfLocation) throws GeocodingApiCallException {
        try {
            URI uri = buildUriForGeocodingRequest(nameOfLocation);
            HttpRequest request = buildRequest(uri);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<LocationApiResponse>>() {
                    }
            );

        } catch (Exception e) {
            throw new GeocodingApiCallException("Issues with calling geocoding api for name = " + nameOfLocation);
        }
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
