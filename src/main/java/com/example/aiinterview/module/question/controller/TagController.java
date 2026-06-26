package com.example.aiinterview.module.question.controller;

import java.util.List;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.question.service.TagService;
import com.example.aiinterview.module.question.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> listTags() {
        return Result.success(tagService.listTags());
    }
}
