package com.login.controller;

import com.login.common.Result;
import com.login.entity.Category;
import com.login.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return categoryService.listAll();
    }

    @PostMapping
    public Result<Void> add(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") Integer sort) {
        return categoryService.add(name, sort);
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Integer sort,
            @RequestParam Integer status) {
        return categoryService.update(id, name, sort, status);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }
}
