package ru.practicum.controllers.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.compilation.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * COMPILATION CONTROLLER
 * processing HTTP-requests to "/compilations" end-point to get compilations of events.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(COMPILATIONS_PATH)
public class CompilationPublicController {

    private final CompilationService compilationService;

    /**
     * Processing a GET-request to the endpoint "/compilations"
     * Get compilations of events
     * no required parameters
     *
     * @param pinned search only pinned/unpinned collections
     * @param from   number of elements that need to be skipped to form the current page, default value = 10
     * @param size   number of elements per page, default value = 10
     * @return List of compilations of events,
     * if no compilations are found based on the specified filters, it returns an empty list
     */

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(name = PINNED_PARAMETER_NAME,
                    required = false) Boolean pinned,
            @PositiveOrZero @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE) Integer from,
            @Positive @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request to the endpoint \"/compilations\".\n"
                + "COMPILATIONS. PUBLIC ACCESS.\n"
                + "Get compilations, starting from: {}, number of compilations: {}, pinned: {}", from, size, pinned);
        return compilationService.getCompilations(from, size, pinned);
    }

    /**
     * processing a GET-request to the endpoint "/compilations/{compId}"
     * to get a compilation by id
     *
     * @param compId compilation id
     * @return compilation
     */
    @GetMapping(COMPILATION_ID_PATH_VARIABLE)
    public CompilationDto getCompilationById(@PathVariable Integer compId) {
        log.info("GET-request to the endpoint \"/compilations/{}\".\n"
                + "COMPILATIONS. PUBLIC ACCESS.\n"
                + "Get compilation by id {}", compId, compId);
        return compilationService.getCompilationById(compId);
    }


}
