package com.example.aiinterview.module.ai.enums;

import java.util.Arrays;

import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;

public enum EnterpriseInterviewerType {

    ALIBABA("Alibaba", "Rigorous technical interviewer", "Deeply probe architecture choices, implementation details, stability, and trade-offs."),
    TENCENT("Tencent", "Comprehensive interviewer", "Balance technical depth, product thinking, collaboration, communication, and engineering judgment."),
    BYTEDANCE("ByteDance", "High-pressure algorithm interviewer", "Use frequent follow-ups, pressure testing, algorithmic thinking, and performance details."),
    STARTUP("Startup", "Practical interviewer", "Focus on fast delivery, real project experience, problem solving, and pragmatic trade-offs.");

    private final String companyName;
    private final String roleName;
    private final String systemPrompt;

    EnterpriseInterviewerType(String companyName, String roleName, String systemPrompt) {
        this.companyName = companyName;
        this.roleName = roleName;
        this.systemPrompt = systemPrompt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public static EnterpriseInterviewerType defaultIfNull(EnterpriseInterviewerType type) {
        return type == null ? STARTUP : type;
    }

    public static EnterpriseInterviewerType parse(String value) {
        if (value == null || value.isBlank()) {
            return STARTUP;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported interviewer type"));
    }
}
