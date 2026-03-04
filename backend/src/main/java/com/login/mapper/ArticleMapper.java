package com.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.login.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
