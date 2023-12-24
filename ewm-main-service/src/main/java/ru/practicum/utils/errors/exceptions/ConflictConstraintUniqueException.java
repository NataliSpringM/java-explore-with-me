package ru.practicum.utils.errors.exceptions;

/**
 * ConflictConstraintUniqueException
 * reporting about database integrity violations
 */

public class ConflictConstraintUniqueException extends RuntimeException {
    public ConflictConstraintUniqueException(String message) {
        super(message);
    }

}
