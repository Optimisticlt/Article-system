package com.login.controller;

import com.login.common.Result;
import com.login.entity.Tag;
import com.login.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return tagService.listAll();
    }

    @PostMapping
    public Result<Void> add(@RequestParam String name) {
        return tagService.add(name);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return tagService.delete(id);
    }
}
