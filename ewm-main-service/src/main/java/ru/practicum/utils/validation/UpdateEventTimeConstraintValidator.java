package ru.practicum.utils.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class UpdateEventTimeConstraintValidator implements ConstraintValidator<TwoHoursLater, LocalDateTime> {

    @Override
    public void initialize(TwoHoursLater constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime time, ConstraintValidatorContext constraintValidatorContext) {
        if (time == null) {
            return true;
        }
        return time.isAfter(LocalDateTime.now().plusHours(2));
    }
}
