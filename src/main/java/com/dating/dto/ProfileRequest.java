package com.dating.dto;

import com.dating.entity.Profile;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class ProfileRequest {
    @NotBlank @Size(max = 50)
    private String name;
    @NotNull @Past
    private LocalDate dateOfBirth;
    @NotNull
    private Profile.Gender gender;
    @NotNull
    private Profile.Gender interestedIn;
    @Size(max = 500)
    private String bio;
    @Size(max = 100)
    private String occupation;
    @Size(max = 100)
    private String education;
    @Min(100) @Max(250)
    private Integer heightCm;
    @Size(max = 50)
    private String religion;
    private Set<String> languages;
    private Double latitude;
    private Double longitude;
    @Size(max = 100)
    private String city;
    @Size(max = 100)
    private String country;
}
