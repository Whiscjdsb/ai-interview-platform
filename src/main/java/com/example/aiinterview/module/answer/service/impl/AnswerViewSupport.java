package com.example.aiinterview.module.answer.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.aiinterview.module.question.dto.QuestionTagPair;
import com.example.aiinterview.module.question.mapper.QuestionTagRelationMapper;
import com.example.aiinterview.module.question.vo.TagVO;
import org.springframework.util.CollectionUtils;

final class AnswerViewSupport {

    private AnswerViewSupport() {
    }

    static Map<Long, List<TagVO>> loadTags(QuestionTagRelationMapper mapper, List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Collections.emptyMap();
        }
        List<QuestionTagPair> pairs = mapper.selectTagPairsByQuestionIds(questionIds);
        return pairs.stream().collect(Collectors.groupingBy(
                QuestionTagPair::getQuestionId,
                Collectors.mapping(pair -> new TagVO(pair.getTagId(), pair.getTagName(), pair.getDescription()), Collectors.toList())));
    }
}
