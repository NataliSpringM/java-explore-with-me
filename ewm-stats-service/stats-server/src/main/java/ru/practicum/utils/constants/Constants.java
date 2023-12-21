package ru.practicum.utils.constants;

import java.time.format.DateTimeFormatter;

/**
 * Constants: path parts, path variables, time-formatting constants
 */
public class Constants {

    /**
     * Path parts constants
     */

    public static final String HIT_PATH = "/hit";
    public static final String STATS_PATH = "/stats";
    public static final String CONTROLLERS_PATH = "ru.practicum.controllers";

    /**
     * Parameters' names and default values constants
     */
    public static final String START_PARAMETER_NAME = "start";
    public static final String END_PARAMETER_NAME = "end";
    public static final String URIS_PARAMETER_NAME = "uris";
    public static final String UNIQUE_PARAMETER_NAME = "unique";
    public static final String FALSE_DEFAULT_VALUE = "false";

    /**
     * Date and time formatting
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    /**
     * Validation messages
     */
    public static final String START_AFTER_END = "Check time interval: start time should be before end time";
    public static final String INCORRECTLY_MADE_REQUEST = "Incorrectly made request.";

}
