package com.example.aiinterview.module.ai.controller;

import java.nio.charset.StandardCharsets;

import com.example.aiinterview.module.ai.service.AiInterviewPdfService;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai/interview")
@RequiredArgsConstructor
public class AiInterviewPdfController {

    private final AiInterviewPdfService aiInterviewPdfService;

    @GetMapping(value = "/{id}/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long id) {
        byte[] pdf = aiInterviewPdfService.generatePdf(principal.getId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("AI面试报告_" + id + ".pdf", StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(pdf);
    }
}
