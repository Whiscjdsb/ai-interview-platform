package com.example.aiinterview.module.ai.controller;

import java.util.List;

import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.ai.dto.InterviewTemplateCreateRequest;
import com.example.aiinterview.module.ai.enums.EnterpriseInterviewerType;
import com.example.aiinterview.module.ai.service.EnterpriseInterviewService;
import com.example.aiinterview.module.ai.vo.EnterpriseFitAnalysisVO;
import com.example.aiinterview.module.ai.vo.InterviewTemplateVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai/interview")
@RequiredArgsConstructor
public class EnterpriseInterviewController {

    private final EnterpriseInterviewService enterpriseInterviewService;

    @PostMapping("/template/create")
    public Result<InterviewTemplateVO> createTemplate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InterviewTemplateCreateRequest request) {
        return Result.success(enterpriseInterviewService.createTemplate(principal.getId(), request));
    }

    @GetMapping("/template/list")
    public Result<List<InterviewTemplateVO>> listTemplates(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(enterpriseInterviewService.listTemplates(principal.getId()));
    }

    @GetMapping("/fit-analysis")
    public Result<EnterpriseFitAnalysisVO> analyzeFit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute EnterpriseFitQuery query) {
        return Result.success(enterpriseInterviewService.analyzeFit(
                principal.getId(),
                query.getPositionModel(),
                query.getCompanyType()));
    }

    public static class EnterpriseFitQuery {

        @Size(max = 128, message = "position model length must be at most 128")
        private String positionModel;

        private EnterpriseInterviewerType companyType;

        public String getPositionModel() {
            return positionModel;
        }

        public void setPositionModel(String positionModel) {
            this.positionModel = positionModel;
        }

        public EnterpriseInterviewerType getCompanyType() {
            return companyType;
        }

        public void setCompanyType(EnterpriseInterviewerType companyType) {
            this.companyType = companyType;
        }
    }
}
