package com.login.controller;

import com.login.common.ArticleVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping("/list")
    public Result<PageResult<ArticleVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return articleService.adminListArticles(page, size, keyword);
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return articleService.adminUpdateStatus(id, status);
    }

    @PutMapping("/top/{id}")
    public Result<Void> toggleTop(@PathVariable Long id) {
        return articleService.adminToggleTop(id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return articleService.adminDeleteArticle(id);
    }
}
