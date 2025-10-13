package com.prajwal.securenote.security.request;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min=3, max=20)
    private String username;
    @NotBlank
    @Size(max=50)
    private String email;
    @NotBlank
    @Size(min=6, max=40)
    private String password;

    @Setter
    @Getter
    private Set<String> roles;
}
