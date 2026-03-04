package com.login.controller;

import com.login.common.PageResult;
import com.login.common.Result;
import com.login.common.UserProfileVO;
import com.login.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserService sysUserService;

    @GetMapping("/list")
    public Result<PageResult<UserProfileVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysUserService.listUsers(page, size);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return sysUserService.updateUserStatus(id, status);
    }
}
