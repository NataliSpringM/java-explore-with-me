package ru.practicum.utils.errors.exceptions;

/**
 * NotFoundException
 * reporting about not existing entities
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
