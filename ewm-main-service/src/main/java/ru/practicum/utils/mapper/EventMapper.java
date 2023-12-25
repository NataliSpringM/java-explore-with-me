package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.*;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.StateAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Map Event entity and DTOs into each other
 */
@UtilityClass
public class EventMapper {
    /**
     * map NewEventDto to Event entity
     */

    public static Event toEventEntity(NewEventDto dto,
                                      Location location,
                                      User initiator,
                                      Category category) {
        return Event.builder()
                .confirmedRequests(0)
                .eventDate(dto.getEventDate())
                .description(dto.getDescription())
                .annotation(dto.getAnnotation())
                .title(dto.getTitle())
                .paid(dto.getPaid() != null && dto.getPaid())
                .participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
                .createdOn(LocalDateTime.now())
                .requestModeration(dto.getRequestModeration() == null || dto.getRequestModeration())
                .category(category)
                .initiator(initiator)
                .location(location)
                .state(EventState.PENDING.name())
                .rating(0L)
                .views(0L)
                .build();
    }

    /**
     * map Event entity to EventFullDto
     */
    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn() == null ? null : event.getPublishedOn())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .confirmedRequests(event.getConfirmedRequests())
                .state(EventState.valueOf(event.getState()))
                .views(event.getViews())
                .rating(event.getRating())
                .build();
    }

    /**
     * map Event entity to EventFullDto
     */
    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .rating(event.getRating())
                .build();
    }


    /**
     * map Event entity to EventShortDto with statistics hits
     */
    public static Event toEventWithStat(Event event, Long hits) {
        return event.toBuilder()
                .views(hits)
                .build();
    }

    /**
     * map list of Event entities into EventShortDto list
     */

    public static List<EventShortDto> toEventShortDtoList(List<Event> events) {
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    /**
     * map list of Event entities into EventFullDto list
     */


    public static List<EventFullDto> toEventFullDtoList(List<Event> events) {
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    /**
     * map EntityFullDto entity to UpdateEventUserRequest
     */
    public static UpdateEventUserRequest toUpdateEventUserRequest(EventFullDto eventFullDto, StateAction action) {
        return UpdateEventUserRequest.builder()
                .annotation(eventFullDto.getAnnotation())
                .category(eventFullDto.getCategory().getId())
                .description(eventFullDto.getDescription())
                .eventDate(eventFullDto.getEventDate())
                .location(eventFullDto.getLocation())
                .paid(eventFullDto.getPaid())
                .participantLimit(eventFullDto.getParticipantLimit())
                .requestModeration(eventFullDto.getRequestModeration())
                .stateAction(action)
                .title(eventFullDto.getTitle())
                .build();
    }

    /**
     * map EntityFullDto entity to UpdateEventUserRequest
     */

    public static UpdateEventAdminRequest toUpdateEventAdminRequest(EventFullDto eventFullDto, StateAction action) {
        return UpdateEventAdminRequest.builder()
                .annotation(eventFullDto.getAnnotation())
                .category(eventFullDto.getCategory().getId())
                .description(eventFullDto.getDescription())
                .eventDate(eventFullDto.getEventDate())
                .location(eventFullDto.getLocation())
                .paid(eventFullDto.getPaid())
                .participantLimit(eventFullDto.getParticipantLimit())
                .requestModeration(eventFullDto.getRequestModeration())
                .stateAction(action)
                .title(eventFullDto.getTitle())
                .build();
    }
}
