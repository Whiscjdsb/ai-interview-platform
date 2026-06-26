package com.example.aiinterview.module.ai.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.ai.dto.InterviewTemplateCreateRequest;
import com.example.aiinterview.module.ai.entity.InterviewTemplate;
import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.ai.mapper.InterviewTemplateMapper;
import com.example.aiinterview.module.ai.service.AiUserGrowthService;
import com.example.aiinterview.module.ai.service.EnterpriseInterviewService;
import com.example.aiinterview.module.ai.vo.AiGrowthDimensionVO;
import com.example.aiinterview.module.ai.vo.AiUserGrowthVO;
import com.example.aiinterview.module.ai.vo.EnterpriseFitAnalysisVO;
import com.example.aiinterview.module.ai.vo.InterviewTemplateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EnterpriseInterviewServiceImpl implements EnterpriseInterviewService {

    private static final Map<String, Integer> DEFAULT_WEIGHTS = Map.of(
            "java", 20,
            "spring", 20,
            "systemDesign", 25,
            "project", 20,
            "communication", 15);

    private final InterviewTemplateMapper interviewTemplateMapper;
    private final AiUserGrowthService aiUserGrowthService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewTemplateVO createTemplate(Long userId, InterviewTemplateCreateRequest request) {
        Map<String, Integer> weights = normalizeWeights(request.getScoringWeights());
        InterviewTemplate template = new InterviewTemplate();
        template.setUserId(userId);
        template.setPositionModel(request.getPositionModel());
        template.setCompanyType(EnterpriseInterviewerType.defaultIfNull(request.getCompanyType()));
        template.setDifficulty(request.getDifficulty());
        template.setQuestionCount(request.getQuestionCount());
        template.setFocusAreas(writeJson(request.getFocusAreas()));
        template.setScoringWeights(writeJson(weights));
        interviewTemplateMapper.insert(template);
        return toVO(template);
    }

    @Override
    public List<InterviewTemplateVO> listTemplates(Long userId) {
        return interviewTemplateMapper.selectList(new LambdaQueryWrapper<InterviewTemplate>()
                        .eq(InterviewTemplate::getUserId, userId)
                        .orderByDesc(InterviewTemplate::getCreateTime))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public EnterpriseFitAnalysisVO analyzeFit(Long userId, String positionModel, EnterpriseInterviewerType companyType) {
        AiUserGrowthVO growth = aiUserGrowthService.getUserGrowth(userId);
        AiGrowthDimensionVO dimension = growth.getDimension();
        if (growth.getInterviewCount() == null || growth.getInterviewCount() == 0 || dimension == null) {
            return new EnterpriseFitAnalysisVO(
                    0,
                    List.of("Java基础", "Spring Boot", "系统设计"),
                    "暂无足够面试数据，建议先完成 1-2 场企业模拟面试后再查看岗位匹配度。");
        }

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Java基础", safe(dimension.getJava()));
        scores.put("Spring Boot", safe(dimension.getSpring()));
        scores.put("数据库", safe(dimension.getDatabase()));
        scores.put("系统设计", safe(dimension.getSystemDesign()));
        scores.put("项目经验", safe(dimension.getProject()));

        int fitScore = Math.round((float) scores.values().stream().mapToInt(Integer::intValue).average().orElse(0D));
        List<String> gapAreas = scores.entrySet().stream()
                .filter(entry -> entry.getValue() < 78)
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
        if (gapAreas.isEmpty()) {
            gapAreas = List.of("高并发场景", "架构权衡表达");
        }

        EnterpriseInterviewerType type = EnterpriseInterviewerType.defaultIfNull(companyType);
        String target = StringUtils.hasText(positionModel) ? positionModel : "Java 后端岗位";
        return new EnterpriseFitAnalysisVO(
                Math.max(0, Math.min(100, fitScore)),
                gapAreas,
                type.getCompanyName() + " 风格下，" + target + " 建议优先补强：" + String.join("、", gapAreas) + "。");
    }

    private InterviewTemplateVO toVO(InterviewTemplate template) {
        return new InterviewTemplateVO(
                template.getId(),
                template.getPositionModel(),
                template.getCompanyType(),
                template.getDifficulty(),
                template.getQuestionCount(),
                readList(template.getFocusAreas()),
                readMap(template.getScoringWeights()),
                template.getCreateTime());
    }

    private Map<String, Integer> normalizeWeights(Map<String, Integer> weights) {
        if (CollectionUtils.isEmpty(weights)) {
            return DEFAULT_WEIGHTS;
        }
        int total = weights.values().stream().mapToInt(value -> value == null ? 0 : value).sum();
        if (total <= 0) {
            return DEFAULT_WEIGHTS;
        }
        return weights;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to serialize interview template");
        }
    }

    private List<String> readList(String content) {
        try {
            if (!StringUtils.hasText(content)) {
                return List.of();
            }
            return objectMapper.readValue(content, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<String, Integer> readMap(String content) {
        try {
            if (!StringUtils.hasText(content)) {
                return DEFAULT_WEIGHTS;
            }
            return objectMapper.readValue(content, new TypeReference<Map<String, Integer>>() {
            });
        } catch (Exception ex) {
            return DEFAULT_WEIGHTS;
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
