package com.login.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String content;

    private String summary;

    private String coverUrl;

    private Integer status;

    private Integer viewCount;

    private Long userId;

    private Long categoryId;

    private Integer isTop;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
