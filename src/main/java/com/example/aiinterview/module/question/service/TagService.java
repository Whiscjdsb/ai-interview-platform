package com.example.aiinterview.module.question.service;

import java.util.List;

import com.example.aiinterview.module.question.dto.TagSaveRequest;
import com.example.aiinterview.module.question.vo.TagVO;

public interface TagService {

    List<TagVO> listTags();

    TagVO createTag(TagSaveRequest request);

    TagVO updateTag(Long id, TagSaveRequest request);

    void deleteTag(Long id);
}
