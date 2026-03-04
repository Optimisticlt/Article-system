package com.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.login.common.Result;
import com.login.entity.Category;
import com.login.mapper.CategoryMapper;
import com.login.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Result<List<Category>> listAll() {
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));
        return Result.success(categories);
    }

    @Override
    public Result<Void> add(String name, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setSort(sort != null ? sort : 0);
        category.setStatus(1);
        category.setIsDeleted(0);
        save(category);
        return Result.success();
    }

    @Override
    public Result<Void> update(Long id, String name, Integer sort, Integer status) {
        Category category = getById(id);
        if (category == null) return Result.error(404, "Category not found");
        if (name != null) category.setName(name);
        if (sort != null) category.setSort(sort);
        if (status != null) category.setStatus(status);
        updateById(category);
        return Result.success();
    }

    @Override
    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success();
    }
}
