package com.example.aiinterview.module.question.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiinterview.module.question.dto.QuestionTagPair;
import com.example.aiinterview.module.question.entity.QuestionTagRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionTagRelationMapper extends BaseMapper<QuestionTagRelation> {

    @Delete("DELETE FROM question_tag_relation WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);

    @Select("""
            <script>
            SELECT
              qtr.question_id AS questionId,
              qt.id AS tagId,
              qt.tag_name AS tagName,
              qt.description AS description
            FROM question_tag_relation qtr
            INNER JOIN question_tag qt ON qt.id = qtr.tag_id
            WHERE qtr.deleted = 0
              AND qt.deleted = 0
              AND qtr.question_id IN
              <foreach collection="questionIds" item="questionId" open="(" separator="," close=")">
                #{questionId}
              </foreach>
            ORDER BY qt.tag_name ASC
            </script>
            """)
    List<QuestionTagPair> selectTagPairsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    @Select("""
            SELECT COUNT(1)
            FROM question_tag_relation qtr
            INNER JOIN question q ON q.id = qtr.question_id
            WHERE qtr.tag_id = #{tagId}
              AND qtr.deleted = 0
              AND q.deleted = 0
            """)
    int countActiveQuestionsByTagId(@Param("tagId") Long tagId);
}
