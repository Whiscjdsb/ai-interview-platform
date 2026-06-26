package com.example.aiinterview.module.question.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.question.dto.TagSaveRequest;
import com.example.aiinterview.module.question.entity.QuestionTag;
import com.example.aiinterview.module.question.mapper.QuestionTagMapper;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.service.TagService;
import com.example.aiinterview.module.question.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final QuestionTagMapper questionTagMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    public List<TagVO> listTags() {
        return questionTagMapper.selectList(new LambdaQueryWrapper<QuestionTag>()
                        .eq(QuestionTag::getDeleted, 0)
                        .orderByAsc(QuestionTag::getTagName))
                .stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO createTag(TagSaveRequest request) {
        ensureTagNameAvailable(request.getTagName(), null);
        QuestionTag tag = new QuestionTag();
        tag.setTagName(request.getTagName());
        tag.setDescription(request.getDescription());
        questionTagMapper.insert(tag);
        adminStatisticsCacheService.evictAll();
        return toVo(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO updateTag(Long id, TagSaveRequest request) {
        QuestionTag tag = getTagOrThrow(id);
        ensureTagNameAvailable(request.getTagName(), id);
        tag.setTagName(request.getTagName());
        tag.setDescription(request.getDescription());
        questionTagMapper.updateById(tag);
        adminStatisticsCacheService.evictAll();
        return toVo(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        getTagOrThrow(id);
        int activeQuestionCount = questionTagRelationMapper.countActiveQuestionsByTagId(id);
        if (activeQuestionCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Tag is used by questions and cannot be deleted");
        }
        questionTagMapper.deleteById(id);
        adminStatisticsCacheService.evictAll();
    }

    private void ensureTagNameAvailable(String tagName, Long excludeId) {
        LambdaQueryWrapper<QuestionTag> wrapper = new LambdaQueryWrapper<QuestionTag>()
                .eq(QuestionTag::getTagName, tagName)
                .eq(QuestionTag::getDeleted, 0)
                .ne(excludeId != null, QuestionTag::getId, excludeId)
                .last("LIMIT 1");
        if (questionTagMapper.selectOne(wrapper) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Tag name already exists");
        }
    }

    private QuestionTag getTagOrThrow(Long id) {
        QuestionTag tag = questionTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag does not exist");
        }
        return tag;
    }

    private TagVO toVo(QuestionTag tag) {
        return new TagVO(tag.getId(), tag.getTagName(), tag.getDescription());
    }
}
