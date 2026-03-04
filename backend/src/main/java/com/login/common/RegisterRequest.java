package com.login.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String userName;
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 30, message = "Password must be 6-30 characters")
    private String passWord;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Captcha is required")
    private String captcha;
    @NotBlank(message = "Captcha key is required")
    private String captchaKey;
}
