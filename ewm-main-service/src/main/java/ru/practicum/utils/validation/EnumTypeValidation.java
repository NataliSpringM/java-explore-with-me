package ru.practicum.utils.validation;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RatingAction;
import ru.practicum.enums.SortType;
import ru.practicum.enums.StateAction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * Enumeration types validation methods
 */
@Slf4j
@UtilityClass
public class EnumTypeValidation {

    /**
     * check whether string is valid StateAdminAction (action for ADMIN)
     * throw exception if isn't
     *
     * @param action StateAction value to check
     * @return StateAction corresponding item
     */
    public static StateAction getValidUserAction(StateAction action) {
        return StateAction.getUserActions().stream()
                .filter(value -> value.equals(action))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_ACTION + action));
    }


    /**
     * check whether string is valid StateUserAction (action for USER)
     * throw exception if isn't
     *
     * @param action StateAction value to check
     * @return StateAction corresponding item
     */
    public static StateAction getValidAdminAction(StateAction action) {
        return StateAction.getAdminActions().stream()
                .filter(value -> value.equals(action))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_ACTION + action));
    }


    /**
     * check whether list elements are valid EventStates
     * throws exception if they qre not
     *
     * @param states List to check
     */
    public static void checkValidEventStates(List<String> states) {
        Optional<String> invalidState = states.stream()
                .filter(state -> {
                    try {
                        EventState.valueOf(state);
                        return false;
                    } catch (IllegalArgumentException e) {
                        return true;
                    }
                })
                .findFirst();
        if (invalidState.isPresent()) {
            throw new IllegalArgumentException(UNKNOWN_EVENT_STATE + invalidState.get());
        }
    }

    /**
     * check whether string is valid SortType
     * throw exception if it is not
     *
     * @param sort String to check
     * @return SortType corresponding item
     */

    public static SortType getValidSortType(String sort) {
        return Arrays.stream(SortType.values())
                .filter(value -> value.name().equalsIgnoreCase(sort))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_SORT_TYPE + sort));
    }

    /**
     * check whether string is valid RatingAction
     * throw exception if it is not
     *
     * @param rating String to check
     * @return RatingAction corresponding item
     */

    public static RatingAction getValidRatingAction(String rating) {
        return Arrays.stream(RatingAction.values())
                .filter(value -> value.name().equalsIgnoreCase(rating))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_RATING_ACTION + rating));
    }
}
