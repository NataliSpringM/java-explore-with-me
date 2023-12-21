package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * EVENT REPOSITORY
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {


    /**
     * Get events for admin
     *
     * @param users      list of user ids whose events need to be found
     * @param states     list of states in which the desired events are located
     * @param categories list of id categories in which the search will be conducted
     * @param pageable   paging parameters
     * @param start      start of time interval to search
     * @param end        end of time interval to search
     * @return complete information about all events that match the passed conditions,
     * if no events are found by the specified filters, returns an empty list
     */
    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.state IN :states) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((cast(:start as timestamp) IS NULL OR e.eventDate >= :start) " +
            "AND ((cast(:end as timestamp) IS NULL OR e.eventDate <= :end)))")
    List<Event> findForAdmin(List<Long> users, List<String> states, List<Long> categories,
                             LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Get events with filtering options for public access.
     * ONLY PUBLISHED events should appear in the results
     * ONLY AVAILABLE to participate events should appear in the results
     *
     * @param text       text to search in the content of the annotation and detailed description of the event
     * @param categories list of category identifiers in which the search will be conducted
     * @param paid       search only for paid/free events
     * @param start      date and time no earlier than which the event should occur
     * @param end        date and time no later than which the event must occur
     * @param pageable   paging parameters
     * @return List of events met filtering criteria.
     * If no events are found by the specified filters, returns an empty list
     */

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND ( e.eventDate >= :start) " +
            "AND ( e.eventDate <= :end) " +
            "AND ( e.state = :published) " +
            "AND ( e.confirmedRequests < e.participantLimit OR e.participantLimit = 0) ")
    List<Event> findAvailableForPublic(String text, List<Long> categories, Boolean paid,
                                       LocalDateTime start, LocalDateTime end, String published, Pageable pageable);

    /**
     * Get events with filtering options for public access.
     * ONLY PUBLISHED  events should appear in the results
     *
     * @param text       text to search in the content of the annotation and detailed description of the event
     * @param categories list of category identifiers in which the search will be conducted
     * @param paid       search only for paid/free events
     * @param start      date and time no earlier than which the event should occur
     * @param end        date and time no later than which the event must occur
     * @param pageable   paging parameters
     * @return List of events met filtering criteria.
     * If no events are found by the specified filters, returns an empty list
     */
    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:text) IS NULL " +
            "OR UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND ((:paid) IS NULL OR e.paid = :paid) " +
            "AND ( e.eventDate >= :start) " +
            "AND ( e.eventDate <= :end) " +
            "AND ( e.state = :published) ")
    List<Event> findAllForPublic(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                 LocalDateTime end,
                                 String published, Pageable pageable);

    /**
     * Get event information by ID and state if exists
     *
     * @param eventId event ID
     * @return detailed event information or empty Optional object
     */
    Optional<Event> findByIdAndState(Long eventId, String state);

    /**
     * Get events by userId and paging parameters
     *
     * @param userId   user id
     * @param pageable paging parameters
     * @return list of events, if no events are found by the specified filters, returns an empty list
     */
    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);


    /**
     * check if events exist by category id
     *
     * @param catId category ID
     * @return true if there are events associated with category
     */
    boolean existsByCategory_Id(Long catId);

    /**
     * find all events by id in list
     *
     * @param ids list events ids
     * @return list of events
     */
    List<Event> findAllByIdIn(List<Long> ids);


}
