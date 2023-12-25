package ru.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * USER entity
 * Long id
 * String name. Must not be blank, min length = 2, max length = 254
 * String email. Must be not blank, unique, min length = 6, max length = 250
 */

@Entity
@Table(name = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_name", nullable = false, length = 250)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "rating")
    private Long rating;
}

