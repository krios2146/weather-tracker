package pet.project.exception;

import java.rmi.ServerException;

public class CookieNotFoundException extends ServerException {
    public CookieNotFoundException(String msg) {
        super(msg);
    }
}
