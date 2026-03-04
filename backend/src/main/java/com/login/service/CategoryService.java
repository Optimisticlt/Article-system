package com.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.login.common.Result;
import com.login.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    Result<List<Category>> listAll();

    Result<Void> add(String name, Integer sort);

    Result<Void> update(Long id, String name, Integer sort, Integer status);

    Result<Void> delete(Long id);
}
