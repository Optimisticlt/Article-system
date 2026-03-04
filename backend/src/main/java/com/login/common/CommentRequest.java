package com.login.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "Article ID is required")
    private Long articleId;

    @NotBlank(message = "Content is required")
    private String content;

    private Long parentId;
}
