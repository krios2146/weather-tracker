package pet.project.exception.authentication;

import jakarta.persistence.EntityExistsException;

public class UserExistsException extends EntityExistsException {
    public UserExistsException(String message) {
        super(message);
    }
}
