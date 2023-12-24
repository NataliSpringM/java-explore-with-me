package ru.practicum.utils.errors;

/**
 * Error messages, reasons
 */
public class ErrorConstants {
    /**
     * Error messages
     */
    public static final String UNKNOWN_SORT_TYPE = "Unknown SortType value:";
    public static final String UNKNOWN_EVENT_STATE = "Unknown EventState value:";
    public static final String UNKNOWN_ACTION = "Unknown StateAction value:";

    public static final String CATEGORY_IS_NOT_EMPTY = "The category is not empty.";
    public static final String EVENT_IS_PUBLISHED = "Cannot publish the event because it's not in the right state:"
            + " PUBLISHED";

    public static final String EVENT_IS_NOT_PUBLISHED_YET = "Access is not allowed";
    public static final String START_AFTER_END = "Check time interval: start time should be before end time";

    public static final String TIME_IS_LESS_THAN_ONE_HOUR_BEFORE_START = "Time limit violation:"
            + " it is less than one hour before event starts";
    public static final String TIME_IS_LESS_THAN_TWO_HOUR_BEFORE_START = "Time limit violation:"
            + " it is less than two hours before event starts";
    public static final String ONLY_FOR_INITIATOR = "Access is allowed only for the initiator of the event";

    public static final String NOT_FOR_INITIATOR = "Access is not allowed for the initiator of the event";

    public static final String LIMIT = "Participation limit has been reached";
    public static final String ONLY_FOR_REQUESTER = "Access is allowed only for the requester";

    public static final String REPEATED_REQUEST = "Only one request from the user is allowed";
    public static final String NOT_PENDING = "List of requests to update contains not pending requests";


    /**
     * Error reasons
     */
    public static final String INCORRECTLY_MADE_REQUEST = "Incorrectly made request.";
    public static final String DATA_INTEGRITY_VIOLATION = "Integrity constraint has been violated.";
    public static final String OBJECT_NOT_FOUND = "The required object was not found.";
    public static final String ACTION_IS_NOT_ALLOWED = "For the requested operation the conditions are not met.";
    /**
     * Error unique constraint violations exceptions
     */
    public static final String CATEGORY_NAME_UNIQUE_VIOLATION = "could not execute statement; "
            + "SQL [n/a]; constraint [uq_category_name]; "
            + "nested exception is org.hibernate.exception.ConstraintViolationException: "
            + "could not execute statement";
    public static final String USER_NAME_UNIQUE_VIOLATION = "could not execute statement; "
            + "SQL [n/a]; constraint [uq_user_name]; "
            + "nested exception is org.hibernate.exception.ConstraintViolationException: "
            + "could not execute statement";
    public static final String COMPILATION_TITLE_UNIQUE_VIOLATION = "could not execute statement; "
            + "SQL [n/a]; constraint [uq_compilation_title]; "
            + "nested exception is org.hibernate.exception.ConstraintViolationException: "
            + "could not execute statement";

    public static String getNotFoundMessage(String name, Number id) {
        return name + " with id=" + id + " was not found";
    }
}
