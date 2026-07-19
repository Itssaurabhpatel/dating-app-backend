package com.dating.dto;

import com.dating.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank @Size(max = 50)
    private String name;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain uppercase, lowercase, number and special character")
    private String password;
    private LocalDate dateOfBirth;
    private User.Gender gender;
    private User.Gender interestedIn;
}
