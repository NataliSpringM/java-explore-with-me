package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Location;

/**
 * LOCATION REPOSITORY
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
