package com.login.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverUrl;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long categoryId;
    private String categoryName;
    private List<TagVO> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }
}
