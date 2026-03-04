package com.login.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String userName;
    @NotBlank(message = "Password is required")
    private String passWord;
    @NotBlank(message = "Captcha is required")
    private String captcha;
    @NotBlank(message = "Captcha key is required")
    private String captchaKey;
    private Boolean rememberMe = false;
}
