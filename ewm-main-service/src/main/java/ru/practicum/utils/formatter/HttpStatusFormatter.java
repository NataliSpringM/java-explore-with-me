package ru.practicum.utils.formatter;

import org.springframework.http.HttpStatus;

/**
 * HttpStatusFormatter
 * format HTTP Status into required format with _ underscore between
 */
public class HttpStatusFormatter {

    public static String formatHttpStatus(HttpStatus status) {
        return status.getReasonPhrase().toUpperCase().replace(" ", "_");
    }
}
