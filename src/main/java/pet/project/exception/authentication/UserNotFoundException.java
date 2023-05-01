package pet.project.exception.authentication;

import jakarta.servlet.ServletException;

public class UserNotFoundException extends ServletException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
