package ru.practicum.dto.user;


import lombok.*;

/**
 * USER DTO details
 * Long id. User ID
 * String name. User name
 * String email. User Email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Long rating;
}