package pet.project.exception;

import jakarta.servlet.ServletException;

public class LocationNotFoundException extends ServletException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
