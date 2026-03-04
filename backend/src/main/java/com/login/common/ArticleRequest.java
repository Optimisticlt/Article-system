package com.login.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArticleRequest {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String summary;
    private String coverUrl;

    @NotNull(message = "Status is required")
    private Integer status;

    private Long categoryId;
    private List<Long> tagIds;
}
