package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.Result;
import com.login.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    Result<List<Tag>> listAll();

    Result<Void> add(String name);

    Result<Void> delete(Long id);
}
