package ru.practicum.service.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Rating;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RatingAction;
import ru.practicum.enums.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.RatingMapper;
import ru.practicum.utils.validation.EnumTypeValidation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * RATING SERVICE IMPLEMENTATION
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RatingRepository ratingRepository;
    private final RequestRepository requestRepository;

    @Override
    public RatingDto addEventRating(Long userId, Long eventId, String rate) {
        RatingAction ratingAction = EnumTypeValidation.getValidRatingAction(rate);
        Event event = getEventOrThrowException(eventId);
        checkEventState(event);
        User user = getUserOrThrowException(userId);
        checkParticipation(userId, eventId);
        try {
            Rating rating = ratingRepository.save(constructEventRating(user, event, ratingAction));
            log.info("New rating: {} added.", rate);
            Long value = Boolean.TRUE.equals(rating.getEventLike()) ? 1L : -1L;
            Event updated = updateEvent(value, event);
            eventRepository.save(updated);
            log.info("Event {} has new rating: {}", eventId, updated.getRating());

            return RatingMapper.toRatingDto(rating);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictConstraintUniqueException(RATING_UNIQUE_VIOLATION);
        }
    }


    @Override
    public RatingDto addInitiatorRating(Long userId, Long initiatorId, String rate) {
        checkUserTryToRateHimself(userId, initiatorId);
        RatingAction ratingAction = EnumTypeValidation.getValidRatingAction(rate);
        User user = getUserOrThrowException(userId);
        User initiator = getUserOrThrowException(initiatorId);
        checkParticipants(userId, initiatorId);
        try {

            Rating rating = ratingRepository.save(constructInitiatorRating(user, initiator, ratingAction));
            log.info("New rating: {} added.", rate);

            Long value = Boolean.TRUE.equals(rating.getInitiatorLike()) ? 1L : -1L;
            User updated = updateInitiator(value, initiator);
            userRepository.save(updated);
            log.info("User {} has new rating as initiator: {}", initiatorId, updated.getRating());

            return RatingMapper.toRatingDto(rating);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictConstraintUniqueException(RATING_UNIQUE_VIOLATION);
        }
    }


    @Override
    public RatingDto deleteInitiatorRating(Long userId, Long initiatorId) {
        checkUserOrThrowException(userId);

        Rating rating = getInitiatorRatingIfExistsOrThrowException(userId, initiatorId);
        log.info("Rating {} deleted", rating);
        ratingRepository.delete(rating);

        Long newValue = Boolean.TRUE.equals(rating.getInitiatorLike()) ? -1L : 1L;
        User initiator = getUserOrThrowException(initiatorId);
        User updated = updateInitiator(newValue, initiator);
        userRepository.save(updated);
        log.info("User {} has new rating as initiator: {}", initiatorId, updated.getRating());

        return RatingMapper.toRatingDto(rating);
    }

    @Override
    public RatingDto deleteEventRating(Long userId, Long eventId) {
        checkUserOrThrowException(userId);

        Rating rating = getEventRatingIfExistsOrThrowException(userId, eventId);
        log.info("Rating {} deleted", rating);
        ratingRepository.delete(rating);

        Long newValue = Boolean.TRUE.equals(rating.getEventLike()) ? -1L : 1L;
        Event event = getEventOrThrowException(eventId);
        Event updated = updateEvent(newValue, event);
        eventRepository.save(updated);
        log.info("Event {} has new rating: {}", eventId, updated.getRating());

        return RatingMapper.toRatingDto(rating);
    }

    /**
     * update Event rating information
     *
     * @param value new rating information
     * @param event event to update
     */
    private Event updateEvent(Long value, Event event) {
        return event.toBuilder()
                .rating(event.getRating() + value)
                .build();
    }


    /**
     * update Initiator rating information
     *
     * @param value new rating information
     * @param user  user to update
     */
    private User updateInitiator(Long value, User user) {
        return user.toBuilder()
                .rating(user.getRating() + value)
                .build();
    }

    /**
     * construct Rating object for event
     *
     * @param user         user
     * @param event        event
     * @param ratingAction ratingAction
     */
    private Rating constructEventRating(User user, Event event, RatingAction ratingAction) {
        Boolean like = ratingAction.equals(RatingAction.LIKE);
        return Rating.builder()
                .user(user)
                .event(event)
                .eventLike(like)
                .build();
    }

    /**
     * construct Rating object for initiator
     *
     * @param user         user
     * @param initiator    initiator
     * @param ratingAction ratingAction
     */
    private Rating constructInitiatorRating(User user, User initiator, RatingAction ratingAction) {

        Boolean like = ratingAction.equals(RatingAction.LIKE);
        return Rating.builder()
                .user(user)
                .initiator(initiator)
                .initiatorLike(like)
                .build();
    }

    /**
     * get Event from repository by id or throw NotFoundException
     *
     * @param eventId event ID
     * @return Event
     */
    private Event getEventOrThrowException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("Event", eventId)));
    }


    /**
     * get User from repository by id or throw NotFoundException
     *
     * @param userId user ID
     * @return User
     */
    private User getUserOrThrowException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("User", userId)));
    }


    /**
     * check is User in repository by id or throw NotFoundException
     *
     * @param userId user ID
     */
    private void checkUserOrThrowException(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new NotFoundException(ErrorConstants.getNotFoundMessage("User", userId));
        }
    }

    /**
     * get if exists rating for event from user
     * throw exception if not
     *
     * @param userId  user ID to check
     * @param eventId event ID
     */
    private Rating getEventRatingIfExistsOrThrowException(Long userId, Long eventId) {
        log.info("We are checking is user with id {}: rate the event {}", userId, eventId);
        Optional<Rating> rating = ratingRepository.findByUser_IdAndEvent_Id(userId, eventId);
        if (rating.isEmpty()) {
            throw new NotFoundException(OBJECT_NOT_FOUND);
        }
        return rating.get();
    }

    /**
     * get if exists rating for event from user
     * throw exception if not
     *
     * @param userId      user ID to check
     * @param initiatorId initiator ID
     */
    private Rating getInitiatorRatingIfExistsOrThrowException(Long userId, Long initiatorId) {
        log.info("We are checking is user with id {}: rate the initiator {}", userId, initiatorId);
        Optional<Rating> rating = ratingRepository.findByUser_IdAndInitiator_Id(userId, initiatorId);
        if (rating.isEmpty()) {
            throw new NotFoundException(OBJECT_NOT_FOUND);
        }
        return rating.get();
    }


    /**
     * get user participation confirmed requests
     *
     * @param userId user ID to check
     */
    private List<Request> getParticipants(Long userId) {
        log.info("We obtain information about participation requests of the user: {}", userId);
        List<Request> list = requestRepository.findAllByRequester_IdAndStatus(userId, RequestStatus.CONFIRMED);
        if (list.isEmpty()) {
            throw new NotAllowedException(ONLY_FOR_PARTICIPANT);
        }
        return list;
    }

    /**
     * check if user was a participant of initiator's events, throws exception if he is not
     *
     * @param userId user ID to check
     */
    private void checkParticipants(Long userId, Long initiatorId) {
        getUserOrThrowException(userId);
        List<Request> requests = getParticipants(userId);
        List<Event> events = requests.stream()
                .map(Request::getEvent)
                .collect(Collectors.toList());
        log.info("User {} is participant in events:", userId);
        ListLogger.logResultList(events);
        checkEventsInitiator(events, initiatorId);
    }


    /**
     * check if user was a participant, throws exception if he is not
     *
     * @param userId  user ID to check
     * @param eventId event ID to check
     */
    private void checkParticipation(Long userId, Long eventId) {
        log.info("We are checking if user with id {}: was a participant of the event {}", userId, eventId);
        if (!requestRepository.existsByRequester_IdAndEvent_IdAndStatus(userId, eventId, RequestStatus.CONFIRMED)) {
            throw new NotAllowedException(ONLY_FOR_PARTICIPANT);
        }
    }

    /**
     * check if event was published
     *
     * @param event event to check
     */
    private void checkEventState(Event event) {
        log.info("We are checking if event {} was published.", event.getId());
        if (!event.getState().equalsIgnoreCase(EventState.PUBLISHED.name())) {
            throw new NotAllowedException(EVENT_IS_NOT_PUBLISHED_YET);
        }
    }

    /**
     * check if events list contains event when initiator was rated user
     *
     * @param events event to check
     */
    private void checkEventsInitiator(List<Event> events, Long initiatorId) {
        log.info("We check if there were events where initiator {} appears", initiatorId);
        events.stream().filter(event -> event
                        .getInitiator().getId()
                        .equals(initiatorId))
                .findAny()
                .orElseThrow(() -> new NotAllowedException(ONLY_FOR_PARTICIPANT));

    }

    /**
     * check if user is initiator
     * throw exception if he is
     *
     * @param userId      user ID
     * @param initiatorId initiator ID
     */

    private void checkUserTryToRateHimself(Long userId, Long initiatorId) {
        if (userId.equals(initiatorId)) {
            throw new NotAllowedException(NOT_FOR_HIMSELF);
        }
    }
}
