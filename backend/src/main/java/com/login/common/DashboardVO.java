package com.login.common;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private Long totalArticles;
    private Long publishedArticles;
    private Long totalComments;
    private Long totalViews;
    private List<Map<String, Object>> categoryStats;
    private List<Map<String, Object>> recentTrend;
    private List<ArticleVO> recentArticles;
}
