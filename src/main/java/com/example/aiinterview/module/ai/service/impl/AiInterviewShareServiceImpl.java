package com.example.aiinterview.module.ai.service.impl;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.ai.service.AiInterviewShareService;
import com.example.aiinterview.module.ai.vo.AiInterviewShareLinkVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiInterviewShareServiceImpl implements AiInterviewShareService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiInterviewShareLinkVO generateShareLink(Long userId, Long interviewId, String baseUrl) {
        AiReviewRecord record = aiReviewRecordMapper.selectOne(new LambdaQueryWrapper<AiReviewRecord>()
                .eq(AiReviewRecord::getId, interviewId)
                .eq(AiReviewRecord::getUserId, userId)
                .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI interview record does not exist");
        }
        ensureSubmitted(record.getResultContent());

        if (!StringUtils.hasText(record.getShareToken())) {
            record.setShareToken(uniqueToken());
        }
        record.setIsPublic(true);
        aiReviewRecordMapper.updateById(record);
        return new AiInterviewShareLinkVO(normalizeBaseUrl(baseUrl) + "/share/" + record.getShareToken());
    }

    @Override
    public AiInterviewSubmitResponseVO getSharedInterview(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Shared interview does not exist");
        }
        AiReviewRecord record = aiReviewRecordMapper.selectOne(new LambdaQueryWrapper<AiReviewRecord>()
                .eq(AiReviewRecord::getShareToken, token)
                .eq(AiReviewRecord::getIsPublic, true)
                .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Shared interview does not exist");
        }
        return parseResult(record.getResultContent());
    }

    private void ensureSubmitted(String resultContent) {
        try {
            JsonNode root = objectMapper.readTree(resultContent);
            if (!root.has("questionResults")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Only submitted interviews can be shared");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only submitted interviews can be shared");
        }
    }

    private AiInterviewSubmitResponseVO parseResult(String resultContent) {
        try {
            ensureSubmitted(resultContent);
            return objectMapper.readValue(resultContent, AiInterviewSubmitResponseVO.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to parse shared interview");
        }
    }

    private String uniqueToken() {
        for (int i = 0; i < 5; i++) {
            String token = UUID.randomUUID() + "-" + randomHex(16);
            Long count = aiReviewRecordMapper.selectCount(new LambdaQueryWrapper<AiReviewRecord>()
                    .eq(AiReviewRecord::getShareToken, token));
            if (count == 0) {
                return token;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate share token");
    }

    private String randomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
