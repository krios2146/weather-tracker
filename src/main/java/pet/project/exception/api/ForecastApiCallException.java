package pet.project.exception.api;

import jakarta.servlet.ServletException;

public class ForecastApiCallException extends ServletException {
    public ForecastApiCallException(String message) {
        super(message);
    }
}
