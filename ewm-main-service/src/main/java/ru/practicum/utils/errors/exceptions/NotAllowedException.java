package ru.practicum.utils.errors.exceptions;

/**
 * ConflictConstraintUniqueException
 * reporting about not allowed operations
 */
public class NotAllowedException extends RuntimeException {
    public NotAllowedException(String message) {
        super(message);
    }

}
