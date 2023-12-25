package ru.practicum.dto.user;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

/**
 * NEW USER request details
 * String name. User's name, must be not empty, min length = 2, max length = 254
 * String email. User Email, must be not empty, in appropriate email format, min length = 6, max length = 250
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewUserRequest {

    @NotBlank(message = "must not be blank")
    @Size(min = 2, max = 250)
    private String name;
    @NotBlank
    @Size(min = 6, max = 254)
    @Email
    private String email;

}