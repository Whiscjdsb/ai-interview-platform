package com.example.aiinterview.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "cannot be blank")
    @Size(min = 4, max = 32, message = "length must be between 4 and 32")
    private String username;

    @NotBlank(message = "cannot be blank")
    @Size(min = 6, max = 64, message = "length must be between 6 and 64")
    private String password;

    @NotBlank(message = "cannot be blank")
    private String confirmPassword;

    @Size(max = 64, message = "length must be at most 64")
    private String nickname;
}
