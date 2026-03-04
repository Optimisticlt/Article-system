package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.Result;
import com.login.entity.ArticleLike;

public interface ArticleLikeService extends IService<ArticleLike> {

    Result<Boolean> toggleLike(Long articleId, Long userId);

    boolean isLiked(Long articleId, Long userId);

    long countByArticleId(Long articleId);
}
