package com.login.controller;

import com.login.common.Result;
import com.login.entity.Tag;
import com.login.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return tagService.listAll();
    }
}
