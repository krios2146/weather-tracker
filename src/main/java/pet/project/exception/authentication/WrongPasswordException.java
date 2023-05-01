package pet.project.exception.authentication;

import jakarta.servlet.ServletException;

public class WrongPasswordException extends ServletException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
