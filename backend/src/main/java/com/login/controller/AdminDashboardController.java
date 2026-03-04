package com.login.controller;

import com.login.common.DashboardVO;
import com.login.common.Result;
import com.login.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final SysUserService sysUserService;

    @GetMapping
    public Result<DashboardVO> dashboard() {
        return sysUserService.getDashboardStats();
    }
}
