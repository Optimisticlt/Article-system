package com.login.controller;

import com.login.common.CommentRequest;
import com.login.common.CommentVO;
import com.login.common.Result;
import com.login.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/list/{articleId}")
    public Result<List<CommentVO>> list(@PathVariable Long articleId) {
        return commentService.listByArticleId(articleId);
    }

    @PostMapping("/add")
    public Result<CommentVO> add(@Valid @RequestBody CommentRequest request) {
        return commentService.addComment(request, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return commentService.deleteComment(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
