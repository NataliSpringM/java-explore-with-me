package ru.practicum.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RATING DTO
 * Long userId. user ID
 * Long eventId. event ID
 * Boolean like. if rating is like
 * Boolean dislike. if rating is dislike
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RatingDto {

    private Long id;

    private Long userId;

    private Long eventId;
    private Long initiatorId;

    private Boolean initiatorLike;

    private Boolean eventLike;

}
