package com.login.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private String content;
    private Long articleId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private LocalDateTime createTime;
    private List<CommentVO> children;
}
