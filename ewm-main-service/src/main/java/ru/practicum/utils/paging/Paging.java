package ru.practicum.utils.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.enums.SortType;

/**
 * construct Pageable objects methods
 */
public class Paging {

    /**
     * construct Pageable without sort
     */
    public static Pageable getPageable(Integer from, Integer size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }

    /**
     * construct Pageable with sort
     */
    public static Pageable getPageable(Integer from, Integer size, SortType sortType) {

        int page = from / size;
        if (sortType == null) {
            return PageRequest.of(page, size);
        } else {
            String fieldName = sortType.name().toLowerCase();
            if (fieldName.equals("event_date")) {
                fieldName = "eventDate";
            }
            if (fieldName.equals("views")) {
                fieldName = "views";
            }
            Sort sort = Sort.by(Sort.Direction.DESC, fieldName);
            return PageRequest.of(page, size, sort);
        }
    }

}
