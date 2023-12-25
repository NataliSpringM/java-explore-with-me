package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.entity.Rating;

/**
 * Map Rating entity and DTOs into each other
 */
@UtilityClass
public class RatingMapper {

    /**
     * map Rating entity into RatingDto
     */

    public static RatingDto toRatingDto(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .eventId(rating.getEvent() == null ? null : rating.getEvent().getId())
                .initiatorId(rating.getInitiator() == null ? null : rating.getInitiator().getId())
                .initiatorLike(rating.getInitiatorLike() == null ? null : rating.getInitiatorLike())
                .eventLike(rating.getEventLike() == null ? null : rating.getEventLike())
                .build();
    }

}

