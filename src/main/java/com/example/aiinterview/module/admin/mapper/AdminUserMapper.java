package com.example.aiinterview.module.admin.mapper;

import java.util.List;

import com.example.aiinterview.module.admin.dto.AdminUserAnswerStatsRow;
import com.example.aiinterview.module.admin.dto.AdminUserDetailStatsRow;
import com.example.aiinterview.module.admin.dto.AdminUserRoleRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminUserMapper {

    @Select("""
            SELECT DISTINCT u.id
            FROM sys_user u
            INNER JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE u.deleted = 0
              AND r.role_code = #{roleCode}
            """)
    List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);

    @Select("""
            <script>
            SELECT ur.user_id AS userId,
                   r.role_code AS roleCode
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE ur.deleted = 0
              AND ur.user_id IN
              <foreach collection="userIds" item="userId" open="(" separator="," close=")">
                #{userId}
              </foreach>
            ORDER BY r.role_code ASC
            </script>
            """)
    List<AdminUserRoleRow> selectRoleRowsByUserIds(@Param("userIds") List<Long> userIds);

    @Select("""
            <script>
            SELECT user_id AS userId,
                   COUNT(1) AS answerCount,
                   MAX(COALESCE(answer_time, create_time)) AS lastAnswerTime
            FROM user_answer_record
            WHERE deleted = 0
              AND user_id IN
              <foreach collection="userIds" item="userId" open="(" separator="," close=")">
                #{userId}
              </foreach>
            GROUP BY user_id
            </script>
            """)
    List<AdminUserAnswerStatsRow> selectAnswerStatsByUserIds(@Param("userIds") List<Long> userIds);

    @Select("""
            SELECT
              (SELECT COUNT(1)
               FROM user_answer_record uar
               WHERE uar.user_id = #{userId}
                 AND uar.deleted = 0) AS answerCount,
              (SELECT COALESCE(SUM(CASE WHEN uar.is_correct = 1 THEN 1 ELSE 0 END), 0)
               FROM user_answer_record uar
               WHERE uar.user_id = #{userId}
                 AND uar.deleted = 0) AS correctCount,
              (SELECT COUNT(1)
               FROM user_favorite uf
               WHERE uf.user_id = #{userId}
                 AND uf.deleted = 0) AS favoriteCount,
              (SELECT COUNT(1)
               FROM user_wrong_question uwq
               WHERE uwq.user_id = #{userId}
                 AND uwq.status = 'ACTIVE'
                 AND uwq.deleted = 0) AS activeWrongQuestionCount,
              (SELECT COUNT(1)
               FROM ai_review_record arr
               WHERE arr.user_id = #{userId}
                 AND arr.record_type = 'ANSWER_REVIEW'
                 AND arr.deleted = 0) AS aiReviewCount
            """)
    AdminUserDetailStatsRow selectUserDetailStats(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(DISTINCT u.id)
            FROM sys_user u
            INNER JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE u.deleted = 0
              AND u.status = 1
              AND r.role_code = 'ADMIN'
            """)
    long countActiveAdmins();
}
