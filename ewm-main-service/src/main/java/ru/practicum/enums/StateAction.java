package ru.practicum.enums;

import java.util.Set;


/**
 * Possible actions for updating event state: [ SEND_TO_REVIEW, CANCEL_REVIEW, PUBLISH_EVENT, REJECT_EVENT ]
 * SEND_TO_REVIEW : action for user to send event to review
 * REJECT_EVENT : action for admin to reject event
 * PUBLISH_EVENT : action for admin to publish event
 * CANCEL_REVIEW : action for user to cancel review
 */
public enum StateAction {
    SEND_TO_REVIEW,
    CANCEL_REVIEW,
    PUBLISH_EVENT,
    REJECT_EVENT;

    /**
     * get only admin allowed actions
     *
     * @return set of Enum values
     */
    public static Set<StateAction> getAdminActions() {
        return Set.of(PUBLISH_EVENT, REJECT_EVENT);
    }

    /**
     * get only user allowed actions
     *
     * @return set of Enum values
     */

    public static Set<StateAction> getUserActions() {
        return Set.of(SEND_TO_REVIEW, CANCEL_REVIEW);
    }
}
