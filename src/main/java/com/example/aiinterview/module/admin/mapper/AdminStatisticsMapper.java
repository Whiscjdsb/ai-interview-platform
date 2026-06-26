package com.example.aiinterview.module.admin.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.aiinterview.module.admin.dto.AdminDailyMetricRow;
import com.example.aiinterview.module.admin.dto.AdminOverviewRow;
import com.example.aiinterview.module.admin.dto.AdminPopularQuestionRow;
import com.example.aiinterview.module.admin.dto.AdminPopularTagRow;
import com.example.aiinterview.module.admin.dto.AdminUserRankingRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminStatisticsMapper {

    @Select("""
            SELECT
              (SELECT COUNT(1)
               FROM sys_user u
               WHERE u.deleted = 0) AS totalUserCount,
              (SELECT COUNT(1)
               FROM sys_user u
               WHERE u.deleted = 0
                 AND u.create_time >= #{todayStart}
                 AND u.create_time < #{tomorrowStart}) AS todayNewUserCount,
              (SELECT COUNT(1)
               FROM question q
               WHERE q.deleted = 0) AS totalQuestionCount,
              (SELECT COUNT(1)
               FROM user_answer_record uar
               WHERE uar.deleted = 0
                 AND uar.create_time >= #{todayStart}
                 AND uar.create_time < #{tomorrowStart}) AS todayAnswerCount,
              (SELECT COUNT(1)
               FROM user_answer_record uar
               WHERE uar.deleted = 0) AS totalAnswerCount,
              (SELECT COALESCE(ROUND(SUM(CASE WHEN uar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(1), 0), 2), 0)
               FROM user_answer_record uar
               WHERE uar.deleted = 0
                 AND uar.is_correct IS NOT NULL) AS averageAccuracyRate,
              (SELECT COUNT(DISTINCT uar.user_id)
               FROM user_answer_record uar
               WHERE uar.deleted = 0
                 AND uar.create_time >= #{activeStart}) AS activeUserCount,
              (SELECT COUNT(1)
               FROM ai_review_record arr
               WHERE arr.deleted = 0
                 AND arr.record_type = 'ANSWER_REVIEW') AS totalAiReviewCount,
              (SELECT COUNT(1)
               FROM ai_review_record arr
               WHERE arr.deleted = 0
                 AND arr.record_type = 'ANSWER_REVIEW'
                 AND arr.create_time >= #{todayStart}
                 AND arr.create_time < #{tomorrowStart}) AS todayAiReviewCount
            """)
    AdminOverviewRow selectOverview(
            @Param("todayStart") LocalDateTime todayStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart,
            @Param("activeStart") LocalDateTime activeStart);

    @Select("""
            SELECT CAST(create_time AS DATE) AS metricDate,
                   COUNT(1) AS count
            FROM sys_user
            WHERE deleted = 0
              AND create_time >= #{startTime}
              AND create_time < #{endExclusive}
            GROUP BY CAST(create_time AS DATE)
            """)
    List<AdminDailyMetricRow> selectNewUsersByDate(
            @Param("startTime") LocalDateTime startTime,
            @Param("endExclusive") LocalDateTime endExclusive);

    @Select("""
            SELECT CAST(create_time AS DATE) AS metricDate,
                   COUNT(1) AS count,
                   COUNT(DISTINCT user_id) AS activeUserCount
            FROM user_answer_record
            WHERE deleted = 0
              AND create_time >= #{startTime}
              AND create_time < #{endExclusive}
            GROUP BY CAST(create_time AS DATE)
            """)
    List<AdminDailyMetricRow> selectAnswersByDate(
            @Param("startTime") LocalDateTime startTime,
            @Param("endExclusive") LocalDateTime endExclusive);

    @Select("""
            SELECT CAST(create_time AS DATE) AS metricDate,
                   COUNT(1) AS count
            FROM ai_review_record
            WHERE deleted = 0
              AND record_type = 'ANSWER_REVIEW'
              AND create_time >= #{startTime}
              AND create_time < #{endExclusive}
            GROUP BY CAST(create_time AS DATE)
            """)
    List<AdminDailyMetricRow> selectAiReviewsByDate(
            @Param("startTime") LocalDateTime startTime,
            @Param("endExclusive") LocalDateTime endExclusive);

    @Select("""
            SELECT q.id AS questionId,
                   q.title AS title,
                   q.question_type AS questionType,
                   q.difficulty AS difficulty,
                   COUNT(uar.id) AS answerCount,
                   COALESCE(ROUND(SUM(CASE WHEN uar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0
                     / NULLIF(SUM(CASE WHEN uar.is_correct IS NOT NULL THEN 1 ELSE 0 END), 0), 2), 0) AS accuracyRate
            FROM user_answer_record uar
            INNER JOIN question q ON q.id = uar.question_id
            WHERE uar.deleted = 0
              AND q.deleted = 0
            GROUP BY q.id, q.title, q.question_type, q.difficulty
            ORDER BY answerCount DESC, q.id ASC
            LIMIT #{limit}
            """)
    List<AdminPopularQuestionRow> selectPopularQuestions(@Param("limit") int limit);

    @Select("""
            SELECT qt.id AS tagId,
                   qt.tag_name AS tagName,
                   COUNT(uar.id) AS answerCount,
                   COUNT(DISTINCT uar.user_id) AS answerUserCount,
                   COALESCE(ROUND(SUM(CASE WHEN uar.is_correct = 1 THEN 1 ELSE 0 END) * 100.0
                     / NULLIF(SUM(CASE WHEN uar.is_correct IS NOT NULL THEN 1 ELSE 0 END), 0), 2), 0) AS averageAccuracyRate
            FROM user_answer_record uar
            INNER JOIN question q ON q.id = uar.question_id AND q.deleted = 0
            INNER JOIN question_tag_relation qtr ON qtr.question_id = q.id AND qtr.deleted = 0
            INNER JOIN question_tag qt ON qt.id = qtr.tag_id AND qt.deleted = 0
            WHERE uar.deleted = 0
            GROUP BY qt.id, qt.tag_name
            ORDER BY answerCount DESC, qt.id ASC
            LIMIT #{limit}
            """)
    List<AdminPopularTagRow> selectPopularTags(@Param("limit") int limit);

    @Select("""
            <script>
            SELECT u.id AS userId,
                   u.username AS username,
                   u.nickname AS nickname,
                   COALESCE(SUM(dlr.answer_count), 0) AS answerCount,
                   COALESCE(SUM(dlr.correct_count), 0) AS correctCount,
                   COALESCE(ROUND(COALESCE(SUM(dlr.correct_count), 0) * 100.0 / NULLIF(COALESCE(SUM(dlr.answer_count), 0), 0), 2), 0) AS accuracyRate,
                   COALESCE(SUM(dlr.study_duration), 0) AS studyDuration
            FROM sys_user u
            INNER JOIN daily_learning_record dlr ON dlr.user_id = u.id
            WHERE u.deleted = 0
              AND dlr.deleted = 0
              AND dlr.record_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY u.id, u.username, u.nickname
            HAVING answerCount > 0
            ORDER BY
            <choose>
              <when test='sortBy == "studyDuration"'>studyDuration DESC, answerCount DESC, u.id ASC</when>
              <when test='sortBy == "accuracyRate"'>accuracyRate DESC, answerCount DESC, u.id ASC</when>
              <otherwise>answerCount DESC, studyDuration DESC, u.id ASC</otherwise>
            </choose>
            LIMIT #{limit}
            </script>
            """)
    List<AdminUserRankingRow> selectUserRanking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("sortBy") String sortBy,
            @Param("limit") int limit);
}
