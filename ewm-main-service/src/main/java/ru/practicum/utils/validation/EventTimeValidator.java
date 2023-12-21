package ru.practicum.utils.validation;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.utils.errors.exceptions.NotAllowedException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static ru.practicum.utils.errors.ErrorConstants.START_AFTER_END;
import static ru.practicum.utils.errors.ErrorConstants.TIME_IS_LESS_THAN_TWO_HOUR_BEFORE_START;

/**
 * Time validations methods
 */
@Slf4j
@UtilityClass
public class EventTimeValidator {

    /**
     * check time for which the event is scheduled {} is not earlier than two hours from the current moment
     * throw exception if it later
     *
     * @param time event time
     **/
    public static void checkStartTimeIsValid(LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new NotAllowedException(TIME_IS_LESS_THAN_TWO_HOUR_BEFORE_START);
        }
    }

    /**
     * check start time is not after end time
     * throw exception if after
     *
     * @param start start time
     * @param end   end time
     **/

    public static void checkStartTimeIsAfterEnd(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException(START_AFTER_END);
        }
    }
}
