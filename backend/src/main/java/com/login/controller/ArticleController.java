package com.login.controller;

import com.login.common.ArticleRequest;
import com.login.common.PageResult;
import com.login.common.ArticleVO;
import com.login.common.Result;
import com.login.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/list")
    public Result<PageResult<ArticleVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return articleService.listArticles(page, size, categoryId, tagId, keyword);
    }

    @GetMapping("/detail/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return articleService.getArticleDetail(id);
    }

    @GetMapping("/search")
    public Result<PageResult<ArticleVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.searchArticles(keyword, page, size);
    }

    @GetMapping("/my")
    public Result<PageResult<ArticleVO>> myArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.listMyArticles(page, size, getCurrentUserId());
    }

    @PostMapping("/publish")
    public Result<ArticleVO> publish(@Valid @RequestBody ArticleRequest request) {
        return articleService.publishArticle(request, getCurrentUserId());
    }

    @PutMapping("/update")
    public Result<ArticleVO> update(@Valid @RequestBody ArticleRequest request) {
        return articleService.updateArticle(request, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return articleService.deleteArticle(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
