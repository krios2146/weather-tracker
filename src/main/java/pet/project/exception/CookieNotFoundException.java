package pet.project.exception;

import jakarta.servlet.ServletException;

public class CookieNotFoundException extends ServletException {
    public CookieNotFoundException(String msg) {
        super(msg);
    }
}
