package com.login.controller;

import com.login.common.LoginRequest;
import com.login.common.RegisterRequest;
import com.login.common.Result;
import com.login.service.CaptchaService;
import com.login.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        return sysUserService.login(request);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        return sysUserService.register(request);
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        return Result.success(captchaService.generateCaptcha());
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestParam String email) {
        return sysUserService.forgotPassword(email);
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, String>> refreshToken(@RequestParam String refreshToken) {
        return sysUserService.refreshToken(refreshToken);
    }
}
