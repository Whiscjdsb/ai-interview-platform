package com.example.aiinterview.module.admin.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.dto.AdminRecordQueryRequest;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewDetailVO;
import com.example.aiinterview.module.admin.service.AdminService;
import com.example.aiinterview.module.admin.vo.AdminAiInterviewRecordVO;
import com.example.aiinterview.module.admin.vo.AdminAnswerRecordVO;
import com.example.aiinterview.module.admin.vo.AdminDashboardVO;
import com.example.aiinterview.module.admin.vo.AdminFavoriteRecordVO;
import com.example.aiinterview.module.admin.vo.AdminWrongQuestionRecordVO;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.answer.entity.UserAnswerRecord;
import com.example.aiinterview.module.answer.entity.UserFavorite;
import com.example.aiinterview.module.answer.entity.UserWrongQuestion;
import com.example.aiinterview.module.answer.mapper.UserAnswerRecordMapper;
import com.example.aiinterview.module.answer.mapper.UserFavoriteMapper;
import com.example.aiinterview.module.answer.mapper.UserWrongQuestionMapper;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.mapper.QuestionMapper;
import com.example.aiinterview.module.user.entity.SysUser;
import com.example.aiinterview.module.user.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysUserMapper sysUserMapper;
    private final QuestionMapper questionMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final UserAnswerRecordMapper userAnswerRecordMapper;
    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AdminDashboardVO getDashboard() {
        return new AdminDashboardVO(
                sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()),
                questionMapper.selectCount(new LambdaQueryWrapper<Question>()),
                userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getDeleted, 0)),
                userWrongQuestionMapper.selectCount(new LambdaQueryWrapper<UserWrongQuestion>().eq(UserWrongQuestion::getDeleted, 0)),
                userAnswerRecordMapper.selectCount(new LambdaQueryWrapper<UserAnswerRecord>()),
                aiReviewRecordMapper.selectCount(new LambdaQueryWrapper<AiReviewRecord>()
                        .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)));
    }

    @Override
    public PageResult<AdminFavoriteRecordVO> pageFavorites(AdminRecordQueryRequest request) {
        List<Long> userIds = userIdsByKeyword(request.getUserKeyword());
        List<Long> questionIds = questionIdsByKeyword(request.getQuestionKeyword());
        if (isEmptySearchResult(request.getUserKeyword(), userIds) || isEmptySearchResult(request.getQuestionKeyword(), questionIds)) {
            return emptyPage(request);
        }
        Page<UserFavorite> page = userFavoriteMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getDeleted, 0)
                        .in(!userIds.isEmpty(), UserFavorite::getUserId, userIds)
                        .in(!questionIds.isEmpty(), UserFavorite::getQuestionId, questionIds)
                        .ge(request.getStartTime() != null, UserFavorite::getCreateTime, request.getStartTime())
                        .le(request.getEndTime() != null, UserFavorite::getCreateTime, request.getEndTime())
                        .orderByDesc(UserFavorite::getCreateTime));
        Map<Long, SysUser> users = loadUsers(page.getRecords().stream().map(UserFavorite::getUserId).toList());
        Map<Long, Question> questions = loadQuestions(page.getRecords().stream().map(UserFavorite::getQuestionId).toList());
        List<AdminFavoriteRecordVO> records = page.getRecords().stream()
                .map(item -> {
                    SysUser user = users.get(item.getUserId());
                    Question question = questions.get(item.getQuestionId());
                    return new AdminFavoriteRecordVO(
                            item.getId(),
                            item.getUserId(),
                            user == null ? null : user.getUsername(),
                            user == null ? null : user.getNickname(),
                            item.getQuestionId(),
                            question == null ? null : question.getTitle(),
                            question == null ? null : question.getQuestionType(),
                            question == null ? null : question.getDifficulty(),
                            item.getCreateTime());
                })
                .toList();
        return toPageResult(page, records);
    }

    @Override
    public PageResult<AdminWrongQuestionRecordVO> pageWrongQuestions(AdminRecordQueryRequest request) {
        List<Long> userIds = userIdsByKeyword(request.getUserKeyword());
        List<Long> questionIds = questionIdsByKeyword(request.getQuestionKeyword());
        if (isEmptySearchResult(request.getUserKeyword(), userIds) || isEmptySearchResult(request.getQuestionKeyword(), questionIds)) {
            return emptyPage(request);
        }
        Page<UserWrongQuestion> page = userWrongQuestionMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<UserWrongQuestion>()
                        .eq(UserWrongQuestion::getDeleted, 0)
                        .in(!userIds.isEmpty(), UserWrongQuestion::getUserId, userIds)
                        .in(!questionIds.isEmpty(), UserWrongQuestion::getQuestionId, questionIds)
                        .eq(request.getStatus() != null, UserWrongQuestion::getStatus, request.getStatus())
                        .ge(request.getStartTime() != null, UserWrongQuestion::getCreateTime, request.getStartTime())
                        .le(request.getEndTime() != null, UserWrongQuestion::getCreateTime, request.getEndTime())
                        .orderByDesc(UserWrongQuestion::getLastWrongTime));
        Map<Long, SysUser> users = loadUsers(page.getRecords().stream().map(UserWrongQuestion::getUserId).toList());
        Map<Long, Question> questions = loadQuestions(page.getRecords().stream().map(UserWrongQuestion::getQuestionId).toList());
        List<AdminWrongQuestionRecordVO> records = page.getRecords().stream()
                .filter(item -> questionTypeMatches(questions.get(item.getQuestionId()), request))
                .map(item -> {
                    SysUser user = users.get(item.getUserId());
                    Question question = questions.get(item.getQuestionId());
                    return new AdminWrongQuestionRecordVO(
                            item.getId(),
                            item.getUserId(),
                            user == null ? null : user.getUsername(),
                            user == null ? null : user.getNickname(),
                            item.getQuestionId(),
                            question == null ? null : question.getTitle(),
                            question == null ? null : question.getQuestionType(),
                            question == null ? null : question.getDifficulty(),
                            item.getWrongCount(),
                            item.getStatus(),
                            item.getLastWrongTime(),
                            item.getCreateTime());
                })
                .toList();
        return toPageResult(page, records);
    }

    @Override
    public PageResult<AdminAnswerRecordVO> pageAnswers(AdminRecordQueryRequest request) {
        List<Long> userIds = userIdsByKeyword(request.getUserKeyword());
        List<Long> questionIds = questionIdsByKeyword(request.getQuestionKeyword());
        if (isEmptySearchResult(request.getUserKeyword(), userIds) || isEmptySearchResult(request.getQuestionKeyword(), questionIds)) {
            return emptyPage(request);
        }
        Page<UserAnswerRecord> page = userAnswerRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<UserAnswerRecord>()
                        .in(!userIds.isEmpty(), UserAnswerRecord::getUserId, userIds)
                        .in(!questionIds.isEmpty(), UserAnswerRecord::getQuestionId, questionIds)
                        .eq(request.getIsCorrect() != null, UserAnswerRecord::getIsCorrect, request.getIsCorrect())
                        .ge(request.getStartTime() != null, UserAnswerRecord::getCreateTime, request.getStartTime())
                        .le(request.getEndTime() != null, UserAnswerRecord::getCreateTime, request.getEndTime())
                        .orderByDesc(UserAnswerRecord::getCreateTime));
        Map<Long, SysUser> users = loadUsers(page.getRecords().stream().map(UserAnswerRecord::getUserId).toList());
        Map<Long, Question> questions = loadQuestions(page.getRecords().stream().map(UserAnswerRecord::getQuestionId).toList());
        List<AdminAnswerRecordVO> records = page.getRecords().stream()
                .filter(item -> questionTypeMatches(questions.get(item.getQuestionId()), request))
                .map(item -> {
                    SysUser user = users.get(item.getUserId());
                    Question question = questions.get(item.getQuestionId());
                    return new AdminAnswerRecordVO(
                            item.getId(),
                            item.getUserId(),
                            user == null ? null : user.getUsername(),
                            user == null ? null : user.getNickname(),
                            item.getQuestionId(),
                            question == null ? null : question.getTitle(),
                            question == null ? null : question.getQuestionType(),
                            question == null ? null : question.getDifficulty(),
                            item.getUserAnswer(),
                            question == null ? null : question.getCorrectAnswer(),
                            question == null ? null : question.getAnalysis(),
                            item.getIsCorrect(),
                            item.getScore(),
                            item.getAnswerDuration(),
                            item.getAnswerTime());
                })
                .toList();
        return toPageResult(page, records);
    }

    @Override
    public AdminAiInterviewDetailVO getAiInterviewDetail(Long id) {
        AiReviewRecord record = aiReviewRecordMapper.selectById(id);
        if (record == null || record.getRecordType() != AiRecordType.MOCK_INTERVIEW) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI 面试记录不存在");
        }
        SysUser user = sysUserMapper.selectById(record.getUserId());
        return new AdminAiInterviewDetailVO(
                record.getId(),
                record.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getNickname(),
                parsePosition(record.getInputContent()),
                record.getScore(),
                record.getModelName(),
                record.getCreateTime(),
                parseResultContent(record.getResultContent()));
    }

    @Override
    public PageResult<AdminAiInterviewRecordVO> pageAiInterviews(AdminRecordQueryRequest request) {
        List<Long> userIds = userIdsByKeyword(request.getUserKeyword());
        if (isEmptySearchResult(request.getUserKeyword(), userIds)) {
            return emptyPage(request);
        }
        Page<AiReviewRecord> page = aiReviewRecordMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                new LambdaQueryWrapper<AiReviewRecord>()
                        .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                        .in(!userIds.isEmpty(), AiReviewRecord::getUserId, userIds)
                        .ge(request.getStartTime() != null, AiReviewRecord::getCreateTime, request.getStartTime())
                        .le(request.getEndTime() != null, AiReviewRecord::getCreateTime, request.getEndTime())
                        .orderByDesc(AiReviewRecord::getCreateTime));
        Map<Long, SysUser> users = loadUsers(page.getRecords().stream().map(AiReviewRecord::getUserId).toList());
        List<AdminAiInterviewRecordVO> records = page.getRecords().stream()
                .map(item -> toAiInterviewRecord(item, users.get(item.getUserId())))
                .filter(item -> !StringUtils.hasText(request.getPositionKeyword())
                        || (item.getPosition() != null && item.getPosition().contains(request.getPositionKeyword())))
                .toList();
        return toPageResult(page, records);
    }

    private AdminAiInterviewRecordVO toAiInterviewRecord(AiReviewRecord record, SysUser user) {
        return new AdminAiInterviewRecordVO(
                record.getId(),
                record.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getNickname(),
                parsePosition(record.getInputContent()),
                record.getScore(),
                record.getModelName(),
                record.getCreateTime());
    }

    private String parsePosition(String inputContent) {
        try {
            if (!StringUtils.hasText(inputContent)) {
                return null;
            }
            GenerateInterviewInput input = objectMapper.readValue(inputContent, GenerateInterviewInput.class);
            return input.getPosition();
        } catch (Exception ex) {
            return null;
        }
    }

    private JsonNode parseResultContent(String resultContent) {
        try {
            if (!StringUtils.hasText(resultContent)) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(resultContent);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private List<Long> userIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .and(wrapper -> wrapper
                                .like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getNickname, keyword)
                                .or()
                                .like(SysUser::getEmail, keyword)))
                .stream()
                .map(SysUser::getId)
                .toList();
    }

    private List<Long> questionIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                        .and(wrapper -> wrapper
                                .like(Question::getTitle, keyword)
                                .or()
                                .like(Question::getContent, keyword)))
                .stream()
                .map(Question::getId)
                .toList();
    }

    private boolean isEmptySearchResult(String keyword, List<Long> ids) {
        return StringUtils.hasText(keyword) && ids.isEmpty();
    }

    private boolean questionTypeMatches(Question question, AdminRecordQueryRequest request) {
        if (question == null) {
            return request.getQuestionType() == null;
        }
        return request.getQuestionType() == null || request.getQuestionType().equals(question.getQuestionType());
    }

    private Map<Long, SysUser> loadUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Question> loadQuestions(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private <T> PageResult<T> toPageResult(Page<?> page, List<T> records) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    private <T> PageResult<T> emptyPage(AdminRecordQueryRequest request) {
        return new PageResult<>(request.getPage(), request.getSize(), 0, 0, List.of());
    }

    private static class GenerateInterviewInput {
        private String position;

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }
}
