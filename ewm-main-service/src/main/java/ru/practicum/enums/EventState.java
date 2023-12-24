package ru.practicum.enums;

/**
 * Enumeration of event lifecycle states [ PENDING, PUBLISHED, CANCELED ]
 * PENDING - event waiting for publishing
 * PUBLISHED - event published by
 * CANCELED - event canceled by initiator
 */
public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED
}
