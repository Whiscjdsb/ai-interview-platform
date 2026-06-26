package com.example.aiinterview.module.statistics.mapper;

import java.time.LocalDate;
import java.util.List;

import com.example.aiinterview.module.statistics.dto.TagAccuracyRow;
import com.example.aiinterview.module.statistics.dto.WrongAnalysisRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatisticsMapper {

    @Select("""
            SELECT qt.id AS tagId,
                   qt.tag_name AS tagName,
                   COUNT(1) AS answerCount,
                   SUM(CASE WHEN uar.is_correct = 1 THEN 1 ELSE 0 END) AS correctCount
            FROM user_answer_record uar
            INNER JOIN question q ON q.id = uar.question_id
            INNER JOIN question_tag_relation qtr ON qtr.question_id = q.id AND qtr.deleted = 0
            INNER JOIN question_tag qt ON qt.id = qtr.tag_id AND qt.deleted = 0
            WHERE uar.user_id = #{userId}
              AND uar.deleted = 0
              AND uar.is_correct IS NOT NULL
              AND q.deleted = 0
            GROUP BY qt.id, qt.tag_name
            ORDER BY answerCount DESC, qt.id ASC
            LIMIT 10
            """)
    List<TagAccuracyRow> selectTopTagAccuracy(@Param("userId") Long userId);

    @Select("""
            SELECT qt.id AS tagId,
                   qt.tag_name AS tagName,
                   COUNT(DISTINCT uwq.question_id) AS wrongQuestionCount,
                   COALESCE(SUM(uwq.wrong_count), 0) AS totalWrongCount
            FROM user_wrong_question uwq
            INNER JOIN question q ON q.id = uwq.question_id
            INNER JOIN question_tag_relation qtr ON qtr.question_id = q.id AND qtr.deleted = 0
            INNER JOIN question_tag qt ON qt.id = qtr.tag_id AND qt.deleted = 0
            WHERE uwq.user_id = #{userId}
              AND uwq.deleted = 0
              AND uwq.status = 'ACTIVE'
              AND q.deleted = 0
            GROUP BY qt.id, qt.tag_name
            ORDER BY wrongQuestionCount DESC, totalWrongCount DESC, qt.id ASC
            LIMIT 5
            """)
    List<WrongAnalysisRow> selectWrongAnalysis(@Param("userId") Long userId);

    @Select("""
            SELECT record_date
            FROM daily_learning_record
            WHERE user_id = #{userId}
              AND record_date <= #{today}
              AND answer_count > 0
              AND deleted = 0
            ORDER BY record_date DESC
            """)
    List<LocalDate> selectLearningDatesBeforeToday(@Param("userId") Long userId, @Param("today") LocalDate today);
}
