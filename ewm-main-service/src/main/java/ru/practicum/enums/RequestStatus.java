package ru.practicum.enums;

/**
 * Possible status for participation request: [ PENDING, CONFIRMED, REJECTED, CANCELED ]
 * PENDING : waiting for confirmation by user, added the event
 * CONFIRMED : confirmed participation request
 * REJECTED : rejected participation request
 * CANCELED : the event requested to participate in has been canceled by the initiator
 */
public enum RequestStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELED

}
