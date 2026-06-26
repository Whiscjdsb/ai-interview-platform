package com.example.aiinterview.module.ai.service;

public interface AiInterviewPdfService {

    byte[] generatePdf(Long userId, Long interviewId);
}
