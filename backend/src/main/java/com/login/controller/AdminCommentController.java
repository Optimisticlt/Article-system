package com.login.controller;

import com.login.common.CommentVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/list")
    public Result<PageResult<CommentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return commentService.adminListComments(page, size);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return commentService.adminDeleteComment(id);
    }
}
