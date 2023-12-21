package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.User;

import java.util.List;

/**
 * USER REPOSITORY
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Get users by ids list and paging parameters
     *
     * @param ids  list of user ids whose events need to be found
     * @param page page to return
     * @return list of users
     */
    List<User> findAllByIdIn(List<Long> ids, Pageable page);
}
