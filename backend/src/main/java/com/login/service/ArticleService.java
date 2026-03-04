package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.ArticleRequest;
import com.login.common.ArticleVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.entity.Article;

public interface ArticleService extends IService<Article> {

    Result<ArticleVO> publishArticle(ArticleRequest request, Long userId);

    Result<ArticleVO> updateArticle(ArticleRequest request, Long userId);

    Result<Void> deleteArticle(Long id, Long userId);

    Result<ArticleVO> getArticleDetail(Long id);

    Result<PageResult<ArticleVO>> listArticles(int page, int size, Long categoryId, Long tagId, String keyword);

    Result<PageResult<ArticleVO>> listMyArticles(int page, int size, Long userId);

    Result<PageResult<ArticleVO>> searchArticles(String keyword, int page, int size);

    // Admin operations
    Result<PageResult<ArticleVO>> adminListArticles(int page, int size, String keyword);

    Result<Void> adminUpdateStatus(Long id, Integer status);

    Result<Void> adminToggleTop(Long id);

    Result<Void> adminDeleteArticle(Long id);
}
