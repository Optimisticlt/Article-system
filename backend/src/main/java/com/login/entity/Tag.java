package com.login.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
