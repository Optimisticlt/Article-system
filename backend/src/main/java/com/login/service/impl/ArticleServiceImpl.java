package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.ArticleRequest;
import com.login.common.ArticleVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.entity.*;
import com.login.mapper.*;
import com.login.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final SysUserMapper sysUserMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleVO> publishArticle(ArticleRequest request, Long userId) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverUrl(request.getCoverUrl());
        article.setStatus(request.getStatus());
        article.setCategoryId(request.getCategoryId());
        article.setUserId(userId);
        article.setViewCount(0);
        article.setIsTop(0);
        article.setIsDeleted(0);
        save(article);
        saveTagRelations(article.getId(), request.getTagIds());
        return Result.success(buildVO(article, true));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleVO> updateArticle(ArticleRequest request, Long userId) {
        Article article = getById(request.getId());
        if (article == null) return Result.error(404, "Article not found");
        if (!article.getUserId().equals(userId)) return Result.error(403, "No permission to edit this article");
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverUrl(request.getCoverUrl());
        article.setStatus(request.getStatus());
        article.setCategoryId(request.getCategoryId());
        updateById(article);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        saveTagRelations(article.getId(), request.getTagIds());
        return Result.success(buildVO(article, true));
    }

    @Override
    public Result<Void> deleteArticle(Long id, Long userId) {
        Article article = getById(id);
        if (article == null) return Result.error(404, "Article not found");
        if (!article.getUserId().equals(userId)) return Result.error(403, "No permission to delete this article");
        removeById(id);
        return Result.success();
    }

    @Override
    public Result<ArticleVO> getArticleDetail(Long id) {
        Article article = getById(id);
        if (article == null) return Result.error(404, "Article not found");
        article.setViewCount(article.getViewCount() + 1);
        updateById(article);
        return Result.success(buildVO(article, true));
    }

    @Override
    public Result<PageResult<ArticleVO>> listArticles(int page, int size, Long categoryId, Long tagId, String keyword) {
        List<Long> tagFilterIds = null;
        if (tagId != null) {
            List<ArticleTag> ats = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId));
            if (ats.isEmpty()) return Result.success(PageResult.of(0, 0, page, size, Collections.emptyList()));
            tagFilterIds = ats.stream().map(ArticleTag::getArticleId).collect(Collectors.toList());
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);
        if (categoryId != null) wrapper.eq(Article::getCategoryId, categoryId);
        if (tagFilterIds != null) wrapper.in(Article::getId, tagFilterIds);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }

        Page<Article> pageResult = this.page(new Page<>(page, size), wrapper);
        List<ArticleVO> vos = pageResult.getRecords().stream()
                .map(a -> buildVO(a, false)).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    @Override
    public Result<PageResult<ArticleVO>> listMyArticles(int page, int size, Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .orderByDesc(Article::getCreateTime);
        Page<Article> pageResult = this.page(new Page<>(page, size), wrapper);
        List<ArticleVO> vos = pageResult.getRecords().stream()
                .map(a -> buildVO(a, false)).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    @Override
    public Result<PageResult<ArticleVO>> searchArticles(String keyword, int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .and(w -> w.like(Article::getTitle, keyword)
                        .or().like(Article::getSummary, keyword)
                        .or().like(Article::getContent, keyword))
                .orderByDesc(Article::getCreateTime);
        Page<Article> pageResult = this.page(new Page<>(page, size), wrapper);
        List<ArticleVO> vos = pageResult.getRecords().stream()
                .map(a -> buildVO(a, false)).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    // ---- admin helpers ----

    @Override
    public Result<PageResult<ArticleVO>> adminListArticles(int page, int size, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        Page<Article> pageResult = this.page(new Page<>(page, size), wrapper);
        List<ArticleVO> vos = pageResult.getRecords().stream()
                .map(a -> buildVO(a, false)).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    @Override
    public Result<Void> adminUpdateStatus(Long id, Integer status) {
        Article article = getById(id);
        if (article == null) return Result.error(404, "Article not found");
        article.setStatus(status);
        updateById(article);
        return Result.success();
    }

    @Override
    public Result<Void> adminToggleTop(Long id) {
        Article article = getById(id);
        if (article == null) return Result.error(404, "Article not found");
        article.setIsTop(article.getIsTop() == 1 ? 0 : 1);
        updateById(article);
        return Result.success();
    }

    @Override
    public Result<Void> adminDeleteArticle(Long id) {
        Article article = getById(id);
        if (article == null) return Result.error(404, "Article not found");
        removeById(id);
        return Result.success();
    }

    // ---- private helpers ----

    private void saveTagRelations(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        tagIds.forEach(tagId -> {
            ArticleTag at = new ArticleTag();
            at.setArticleId(articleId);
            at.setTagId(tagId);
            articleTagMapper.insert(at);
        });
    }

    private ArticleVO buildVO(Article article, boolean includeContent) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        if (!includeContent) vo.setContent(null);

        SysUser author = sysUserMapper.selectById(article.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUserName());
            vo.setAuthorAvatar(author.getAvatar());
        }

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) vo.setCategoryName(category.getName());
        }

        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        List<ArticleVO.TagVO> tagVOs = articleTags.stream().map(at -> {
            Tag tag = tagMapper.selectById(at.getTagId());
            ArticleVO.TagVO tagVO = new ArticleVO.TagVO();
            if (tag != null) {
                tagVO.setId(tag.getId());
                tagVO.setName(tag.getName());
            }
            return tagVO;
        }).filter(t -> t.getName() != null).collect(Collectors.toList());
        vo.setTags(tagVOs);

        long likeCount = articleLikeMapper.selectCount(
                new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId, article.getId()));
        vo.setLikeCount((int) likeCount);

        long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, article.getId()));
        vo.setCommentCount((int) commentCount);

        return vo;
    }
}
