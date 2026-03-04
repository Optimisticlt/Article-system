package com.login.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_like")
public class ArticleLike {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long articleId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
