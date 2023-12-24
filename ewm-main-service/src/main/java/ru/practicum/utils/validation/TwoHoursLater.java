package ru.practicum.utils.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation to check EventDate is valid to update
 */
@Target(value = {ElementType.PARAMETER, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UpdateEventTimeConstraintValidator.class)
public @interface TwoHoursLater {

    String message() default "{должно содержать дату, которая еще не наступила.}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

