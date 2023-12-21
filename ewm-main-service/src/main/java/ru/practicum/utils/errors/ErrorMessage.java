package ru.practicum.utils.errors;

import lombok.*;

/**
 * ErrorMessage
 * format to inform about errors with details (rejected field, description of the error, rejected value)
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
    String field;
    String error;
    Object value;

    @Override
    public String toString() {
        return "Field: " + field + ". Error: " + error + ". Value: " + value;
    }
}
