package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.Request;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Map Request entity to ParticipationRequestDto
 */
@UtilityClass
public class RequestMapper {

    /**
     * map Request entity to ParticipationRequestDto
     */
    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }

    /**
     * map list of Request entities into ParticipationRequestDto list
     */

    public static List<ParticipationRequestDto> toParticipationRequestDtoList(List<Request> users) {
        return users.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

}
