package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entities.Hit;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORY
 * storing user requests statistics
 */
@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {
    /**
     * find all visits for specified uris and time interval
     *
     * @param uris  list of uris
     * @param start start of the time interval
     * @param end   end of the time interval
     * @return list of user requests met specified criteria
     */
    List<Hit> findAllByUriInAndTimestampBetween(List<String> uris, LocalDateTime start, LocalDateTime end);

    /**
     * find all visits by specified time interval
     *
     * @param start start of the time interval
     * @param end   end of the time interval
     * @return list of user requests met specified criteria
     */
    List<Hit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

}
