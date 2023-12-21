package ru.practicum.utils.formatter;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * Formatter for localDateTime
 * format date into "yyyy-MM-dd HH:mm:ss"
 */
public class DateTimeFormatter {
    public static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

}
