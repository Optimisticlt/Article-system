package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.CommentRequest;
import com.login.common.CommentVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    Result<CommentVO> addComment(CommentRequest request, Long userId);

    Result<List<CommentVO>> listByArticleId(Long articleId);

    Result<Void> deleteComment(Long id, Long userId);

    // Admin operations
    Result<PageResult<CommentVO>> adminListComments(int page, int size);

    Result<Void> adminDeleteComment(Long id);
}
