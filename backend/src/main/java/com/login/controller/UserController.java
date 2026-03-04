package com.login.controller;

import com.login.common.Result;
import com.login.common.UserProfileVO;
import com.login.entity.SysUser;
import com.login.service.SysUserService;
import com.login.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
        if (token == null || !jwtUtil.isTokenValid(token)) return Result.error(401, "Not logged in or token expired");
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = sysUserService.getById(userId);
        if (user == null) return Result.error(404, "User not found");
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("userName", user.getUserName());
        info.put("email", user.getEmail());
        info.put("status", user.getStatus());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("role", user.getRole());
        info.put("createTime", user.getCreateTime());
        return Result.success(info);
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getProfile(@PathVariable Long userId) {
        return sysUserService.getUserProfile(userId);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email) {
        return sysUserService.updateProfile(getCurrentUserId(), nickname, email);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestParam String oldPwd,
            @RequestParam String newPwd) {
        return sysUserService.updatePassword(getCurrentUserId(), oldPwd, newPwd);
    }

    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestParam String avatarUrl) {
        return sysUserService.updateAvatar(getCurrentUserId(), avatarUrl);
    }

    private Long getCurrentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
