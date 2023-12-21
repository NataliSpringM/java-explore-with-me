package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.CompilationMapper;
import ru.practicum.utils.paging.Paging;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.utils.errors.ErrorConstants.COMPILATION_TITLE_UNIQUE_VIOLATION;

/**
 * USER SERVICE implementation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    /**
     * Add a new collection of events (the collection may not contain events)
     *
     * @param compilation compilation details to add
     *                    (events: List of event identifiers included in the collection, pinned flag, title (required))
     * @return new compilation
     */
    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto compilation) {

        List<Event> events = compilation.getEvents() == null ? new ArrayList<>() : getEvents(compilation.getEvents());
        try {
            Compilation newCompilation = compilationRepository
                    .save(CompilationMapper.toCompilationEntity(compilation, events));
            log.info("New compilation: {} added", newCompilation);
            return CompilationMapper.toCompilationDto(newCompilation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictConstraintUniqueException(COMPILATION_TITLE_UNIQUE_VIOLATION);
        }
    }

    /**
     * Update information about a selection of events.
     * If the field is not specified in the request (equal to null), then changing this data is not required.
     *
     * @param request data of the compilation to be updated
     * @return updated compilation
     */
    @Override
    @Transactional
    public CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest request) {

        Compilation compilation = compilationRepository.getReferenceById(compId);
        List<Event> events;

        if (request.getPinned() != null) {
            compilation = compilation.toBuilder().pinned(request.getPinned()).build();
        }
        if (request.getTitle() != null) {
            compilation = compilation.toBuilder().title(request.getTitle()).build();
        }
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = getEvents(request.getEvents());
            compilation = compilation.toBuilder().events(events).build();
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Compilation: {} updated", updatedCompilation);

        return CompilationMapper.toCompilationDto(updatedCompilation);
    }

    /**
     * Delete compilation
     * Throw exception if compilation to delete was not found
     *
     * @param compId id of the compilation to be deleted
     */
    @Override
    @Transactional
    public CompilationDto deleteCompilation(Integer compId) {
        CompilationDto deleted = CompilationMapper.toCompilationDto(getCompilationOrThrowException(compId));
        compilationRepository.deleteById(compId);
        log.info("Delete compilation with id: {}, compilation: {}", compId, deleted);
        return deleted;
    }

    /**
     * Get List of compilations of the events
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return List of compilations of events.
     * If no compilations are found based on the specified filters, it returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Integer from, Integer size, Boolean pinned) {
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(Paging.getPageable(from, size)).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned);
        }
        ListLogger.logResultList(compilations);
        return CompilationMapper.toCompilationDtoList(compilations);
    }

    /**
     * get Compilation by id
     *
     * @param compId compilation id
     * @return compilation
     */
    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Integer compId) {
        Compilation compilation = getCompilationOrThrowException(compId);
        log.info("Compilation {} was found by id {}", compilation, compId);
        return CompilationMapper.toCompilationDto(compilation);
    }

    /**
     * get Events from CompilationDto object
     */
    private List<Event> getEvents(List<Long> ids) {
        return eventRepository.findAllByIdIn(ids);
    }

    /**
     * get Compilation from repository by id or throw NotFoundException
     *
     * @param compilationId compilation ID
     * @return Compilation
     */
    private Compilation getCompilationOrThrowException(Integer compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorConstants.getNotFoundMessage("Compilation", compilationId)));
    }
}
