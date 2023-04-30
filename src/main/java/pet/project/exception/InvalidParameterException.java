package pet.project.exception;

import jakarta.servlet.ServletException;

public class InvalidParameterException extends ServletException {
    public InvalidParameterException(String message) {
        super(message);
    }
}
