package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Compilation;

import java.util.List;

/**
 * COMPILATION REPOSITORY
 */
@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Integer> {
    /**
     * find events by pinned flag
     *
     * @param pinned boolean pinned
     * @return list of compilations
     */
    List<Compilation> findAllByPinned(Boolean pinned);
}
