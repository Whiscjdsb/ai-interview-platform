package com.example.aiinterview.module.question.controller;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.question.dto.TagSaveRequest;
import com.example.aiinterview.module.question.service.TagService;
import com.example.aiinterview.module.question.vo.TagVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @PostMapping
    public Result<TagVO> createTag(@Valid @RequestBody TagSaveRequest request) {
        return Result.success(tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public Result<TagVO> updateTag(@PathVariable Long id, @Valid @RequestBody TagSaveRequest request) {
        return Result.success(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success(null);
    }
}
