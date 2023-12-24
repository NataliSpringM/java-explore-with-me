package ru.practicum.repository;

import ru.practicum.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CATEGORY REPOSITORY
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * check if category with given name and other id exists
     *
     * @param name  category unique name
     * @param catId category id
     * @return boolean value
     */
    boolean existsByNameAndIdNot(String name, Long catId);

}
