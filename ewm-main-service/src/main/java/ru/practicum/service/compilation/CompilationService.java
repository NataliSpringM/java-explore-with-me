package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

/**
 * COMPILATION SERVICE interface
 */
public interface CompilationService {

    /**
     * Add a new collection of events (the collection may not contain events)
     *
     * @param compilation compilation details to add
     *                    (events: List of event identifiers included in the collection, pinned flag, title (required))
     * @return new compilation
     */
    CompilationDto addCompilation(NewCompilationDto compilation);

    /**
     * Update information about a selection of events.
     * If the field is not specified in the request (equal to null), then changing this data is not required.
     *
     * @param compilation data of the compilation to be updated
     * @return updated compilation
     */

    CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest compilation);

    /**
     * Delete compilation
     *
     * @param compId id of the compilation to be deleted
     */
    CompilationDto deleteCompilation(Integer compId);

    /**
     * Get List of compilations of the events
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return List of compilations of events.
     * If no compilations are found based on the specified filters, it returns an empty list
     */
    List<CompilationDto> getCompilations(Integer from, Integer size, Boolean pinned);

    /**
     * get Compilation by id
     *
     * @param compId compilation id
     * @return compilation
     */
    CompilationDto getCompilationById(Integer compId);
}
