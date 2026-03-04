package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.Result;
import com.login.entity.ArticleLike;
import com.login.mapper.ArticleLikeMapper;
import com.login.service.ArticleLikeService;
import org.springframework.stereotype.Service;

@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements ArticleLikeService {

    @Override
    public Result<Boolean> toggleLike(Long articleId, Long userId) {
        ArticleLike existing = getOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId));
        if (existing != null) {
            removeById(existing.getId());
            return Result.success(false); // unliked
        } else {
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            save(like);
            return Result.success(true); // liked
        }
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        return count(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)) > 0;
    }

    @Override
    public long countByArticleId(Long articleId) {
        return count(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId));
    }
}
