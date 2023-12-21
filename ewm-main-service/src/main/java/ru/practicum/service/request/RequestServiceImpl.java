package ru.practicum.service.request;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.RequestMapper;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * EVENT SERVICE IMPLEMENTATION
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    /**
     * Add a request from a user to participate in an event
     * cannot add a repeat request (Expecting error code 409)
     * the event initiator cannot add a request to participate in his event (Error code 409 expected)
     * cannot participate in an unpublished event (Error code 409 expected)
     * if the event has reached the limit of requests for participation, an error must be returned
     * (Error code 409 is expected)
     * if pre-moderation of participation requests is disabled for an event,
     * then the request should automatically switch to the confirmed state
     *
     * @param userId  user id
     * @param eventId event id
     * @return new participation request
     */
    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {

        checkRequestNotExists(userId, eventId);
        User requester = getUserOrThrowException(userId);
        Event event = getEventOrThrowException(eventId);
        checkEventIsPublished(event.getState());
        checkUserIsNotInitiator(userId, event.getInitiator().getId());
        Integer limit = event.getParticipantLimit();
        Integer confirmed = event.getConfirmedRequests();
        log.info("We have request to event with limit: {}, confirmed: {}", limit, confirmed);
        checkParticipationLimitHasNotReached(limit, confirmed);

        Request request = buildRequest(requester, event);
        request = confirmRequestIfEventHasNoLimits(request, limit, event.getRequestModeration());
        Request newRequest = requestRepository.save(request);
        if (newRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            eventRepository.save(event.toBuilder().confirmedRequests(++confirmed).build());
        }
        log.info("Request {} by requester {} added, status is {},  event: {},  number of confirmed requests is {}",
                request.getId(), userId, newRequest.getStatus(), eventId, confirmed);
        return RequestMapper.toParticipationRequestDto(newRequest);

    }


    /**
     * Cancel own request to participate in an event
     *
     * @param userId    user id
     * @param requestId request id
     * @return canceled participation request
     */
    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {

        Request request = requestRepository.getReferenceById(requestId);
        checkUserIsRequester(userId, request.getRequester().getId());
        Request canceledRequest = requestRepository.save(request.toBuilder().status(RequestStatus.CANCELED).build());
        log.info("Request {} canceled", canceledRequest);
        return RequestMapper.toParticipationRequestDto(canceledRequest);
    }

    /**
     * get information about user requests to participate in other people's events
     *
     * @param userId user id
     * @return list of requests, if no request is found based on the specified filters, it returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        checkUserExists(userId);
        log.info("A request is being made to obtain a list of users' requests");
        List<Request> requests = requestRepository.findAllByRequester_Id(userId);
        ListLogger.logResultList(requests);
        return RequestMapper.toParticipationRequestDtoList(requests);
    }

    /**
     * check if user exists
     * throw exception if he doesn't
     *
     * @param userId user ID
     */

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format("User %s not found", userId));
        }
    }

    /**
     * check Event is published
     * throw exception if it's not
     *
     * @param state Event state
     */
    private void checkEventIsPublished(String state) {
        if (!String.valueOf(EventState.PUBLISHED).equalsIgnoreCase(state)) {
            throw new NotAllowedException(EVENT_IS_NOT_PUBLISHED_YET);
        }
    }

    /**
     * check Request does not exist
     *
     * @param userId  user ID
     * @param eventId event ID
     */

    private void checkRequestNotExists(Long userId, Long eventId) {
        if (requestRepository
                .findAllByRequester_IdAndEvent_Id(userId, eventId)
                .stream()
                .findAny()
                .isPresent()) {
            throw new NotAllowedException(REPEATED_REQUEST);
        }
    }

    /**
     * check participants limit has not reached
     * throw exception if it has
     *
     * @param participantLimit  participantLimit
     * @param confirmedRequests confirmedRequests
     */

    private void checkParticipationLimitHasNotReached(Integer participantLimit, Integer confirmedRequests) {
        if (participantLimit != 0 && participantLimit.equals(confirmedRequests)) {
            throw new NotAllowedException(LIMIT);
        }
    }

    /**
     * check if user is requester
     * throw exception if he is not
     *
     * @param userId      user ID
     * @param requesterId requester ID
     */

    private void checkUserIsRequester(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            throw new NotAllowedException(ONLY_FOR_REQUESTER);
        }
    }

    /**
     * check if User is not initiator
     * throws exception if he is
     *
     * @param userId      user ID
     * @param initiatorId initiator ID
     */
    private void checkUserIsNotInitiator(Long userId, Long initiatorId) {
        if (userId.equals(initiatorId)) {
            throw new NotAllowedException(NOT_FOR_INITIATOR);
        }
    }

    /**
     * build ParticipationRequest
     *
     * @param requester requester
     * @param event     event
     * @return Request
     */
    private Request buildRequest(User requester, Event event) {
        return Request.builder()
                .requester(requester)
                .event(event)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    /**
     * confirm request if no limits are not set
     *
     * @param request           request
     * @param participantLimit  participationLimit
     * @param requestModeration requestModeration required
     * @return request with confirmed status is it is allowed
     */
    private Request confirmRequestIfEventHasNoLimits(Request request, Integer participantLimit,
                                                     Boolean requestModeration) {

        if (participantLimit == 0 || Boolean.FALSE.equals(requestModeration)) {
            log.info("As limit is not set and moderation is false, new status of the request from {}"
                            + " to participate in the event {} is {}",
                    request.getRequester(), request.getEvent(), RequestStatus.CONFIRMED.name());
            return request.toBuilder().status(RequestStatus.CONFIRMED).build();
        }
        return request;
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


}
