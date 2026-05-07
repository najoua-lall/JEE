package com.stage.backend.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter @Setter
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private Set<String> roles;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
