package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.Result;
import com.login.entity.Tag;
import com.login.mapper.TagMapper;
import com.login.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public Result<List<Tag>> listAll() {
        List<Tag> tags = list(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName));
        return Result.success(tags);
    }

    @Override
    public Result<Void> add(String name) {
        long count = count(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (count > 0) return Result.error(400, "Tag already exists");
        Tag tag = new Tag();
        tag.setName(name);
        save(tag);
        return Result.success();
    }

    @Override
    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success();
    }
}
