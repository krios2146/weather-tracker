package pet.project.exception;

import jakarta.servlet.ServletException;

public class UnauthorizedSearchException extends ServletException {
    public UnauthorizedSearchException(String message) {
        super(message);
    }
}
