package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.entity.Rating;

import java.util.Optional;

/**
 * RATING REPOSITORY
 */
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * get rating by userId and eventId
     *
     * @param userId  userId
     * @param eventId eventId
     * @return optional rating
     */
    Optional<Rating> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    /**
     * get rating by userId and initiatorId
     *
     * @param userId      userId
     * @param initiatorId initiatorId
     * @return optional rating
     */
    Optional<Rating> findByUser_IdAndInitiator_Id(Long userId, Long initiatorId);
}
