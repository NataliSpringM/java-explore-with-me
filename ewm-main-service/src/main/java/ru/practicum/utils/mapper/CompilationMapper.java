package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Map Compilation entity and DTOs into each other
 */
@UtilityClass
public class CompilationMapper {
    /**
     * map NewCompilationDto into Compilation entity
     */
    public static Compilation toCompilationEntity(NewCompilationDto compilation, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(compilation.getPinned() != null && compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    /**
     * map Compilation entity into CompilationDTO
     */

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getCompilationId())
                .events(EventMapper.toEventShortDtoList(compilation.getEvents()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }


    /**
     * map list of Compilation entities into CompilationDTO list
     */

    public static List<CompilationDto> toCompilationDtoList(List<Compilation> compilations) {
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }
}
