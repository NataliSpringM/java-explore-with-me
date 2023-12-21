package ru.practicum.controllers.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

import javax.validation.Valid;

import static ru.practicum.utils.constants.Constants.*;

/**
 * COMPILATION ADMIN CONTROLLER, processing HTTP-requests to the endpoint "/admin/compilations"
 * Private ADMIN access API for working with compilations of the events
 */

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ADMIN_PATH + COMPILATIONS_PATH)
public class CompilationAdminController {

    private final CompilationService compilationService;

    /**
     * Processing POST-request to the endpoint "/admin/compilations"
     * Add a new collection of events (the collection may not contain events)
     *
     * @param compilation compilation details to add
     *                    (events: List of event identifiers included in the collection, pinned flag, title (required))
     * @return new compilation
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto compilation) {
        log.info("POST-request to the endpoint \"/admin/compilations\".\n"
                + "COMPILATIONS. ADMIN ACCESS.\n"
                + "Add new compilation of the events: {}", compilation);
        return compilationService.addCompilation(compilation);
    }

    /**
     * Processing PATCH-request to the endpoint "/admin/compilations/{compId}"
     * Change information about a selection of events.
     * If the field is not specified in the request (equal to null), then changing this data is not required.
     *
     * @param compilation data of the compilation to be updated
     * @return updated compilation
     */
    @PatchMapping(COMPILATION_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Integer compId,
                                            @Valid @RequestBody UpdateCompilationRequest compilation) {
        log.info("PATCH-request to the endpoint \"/admin/compilations/{}\".\n"
                + "COMPILATIONS. ADMIN ACCESS.\n"
                + "Update compilation {} by id: {}", compId, compilation, compId);
        return compilationService.updateCompilation(compId, compilation);
    }

    /**
     * Processing DELETE-request to the endpoint "/admin/compilations/{compId}"
     * Delete compilation
     *
     * @param compId id of the compilation to be deleted
     */
    @DeleteMapping(COMPILATION_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CompilationDto deleteCompilation(@PathVariable Integer compId) {
        log.info("DELETE-request to the endpoint \"/admin/compilations/{}\".\n"
                + "COMPILATIONS. ADMIN ACCESS.\n"
                + "Deleting compilation by id: {}", compId, compId);
        return compilationService.deleteCompilation(compId);
    }

}
