package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.*;
import com.login.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    Result<Map<String, String>> login(LoginRequest request);

    Result<Void> register(RegisterRequest request);

    Result<Void> forgotPassword(String email);

    Result<Map<String, String>> refreshToken(String refreshToken);

    Result<UserProfileVO> getUserProfile(Long userId);

    Result<Void> updateProfile(Long userId, String nickname, String email);

    Result<Void> updatePassword(Long userId, String oldPwd, String newPwd);

    Result<Void> updateAvatar(Long userId, String avatarUrl);

    Result<PageResult<UserProfileVO>> listUsers(int page, int size);

    Result<Void> updateUserStatus(Long userId, Integer status);

    Result<DashboardVO> getDashboardStats();
}
