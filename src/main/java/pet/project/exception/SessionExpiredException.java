package pet.project.exception;

import jakarta.servlet.ServletException;

public class SessionExpiredException extends ServletException {
    public SessionExpiredException(String message) {
        super(message);
    }
}
