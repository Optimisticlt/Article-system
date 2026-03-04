package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.CommentRequest;
import com.login.common.CommentVO;
import com.login.common.PageResult;
import com.login.common.Result;
import com.login.entity.Comment;
import com.login.entity.SysUser;
import com.login.mapper.CommentMapper;
import com.login.mapper.SysUserMapper;
import com.login.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final SysUserMapper sysUserMapper;

    @Override
    public Result<CommentVO> addComment(CommentRequest request, Long userId) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setIsDeleted(0);
        save(comment);
        return Result.success(toVO(comment));
    }

    @Override
    public Result<List<CommentVO>> listByArticleId(Long articleId) {
        List<Comment> all = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .orderByAsc(Comment::getCreateTime));

        List<CommentVO> vos = all.stream().map(this::toVO).collect(Collectors.toList());

        // build tree: top-level + attach children
        Map<Long, CommentVO> voMap = vos.stream().collect(Collectors.toMap(CommentVO::getId, v -> v));
        List<CommentVO> roots = new ArrayList<>();
        for (CommentVO vo : vos) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                CommentVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo); // orphaned reply → show as top-level
                }
            }
        }
        return Result.success(roots);
    }

    @Override
    public Result<Void> deleteComment(Long id, Long userId) {
        Comment comment = getById(id);
        if (comment == null) return Result.error(404, "Comment not found");
        if (!comment.getUserId().equals(userId)) return Result.error(403, "No permission to delete this comment");
        removeById(id);
        return Result.success();
    }

    @Override
    public Result<PageResult<CommentVO>> adminListComments(int page, int size) {
        Page<Comment> pageResult = this.page(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime));
        List<CommentVO> vos = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getPages(), page, size, vos));
    }

    @Override
    public Result<Void> adminDeleteComment(Long id) {
        Comment comment = getById(id);
        if (comment == null) return Result.error(404, "Comment not found");
        removeById(id);
        return Result.success();
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setCreateTime(comment.getCreateTime());
        SysUser user = sysUserMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUserName());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }
}
