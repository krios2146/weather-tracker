package pet.project.exception.api;

import jakarta.servlet.ServletException;

public class GeocodingApiCallException extends ServletException {
    public GeocodingApiCallException(String message) {
        super(message);
    }
}
