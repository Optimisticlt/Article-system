package com.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.login.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
