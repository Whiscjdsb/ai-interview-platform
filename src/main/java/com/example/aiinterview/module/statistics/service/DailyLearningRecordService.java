package com.example.aiinterview.module.statistics.service;

import java.time.LocalDate;

public interface DailyLearningRecordService {

    void increaseDailyLearning(Long userId, LocalDate recordDate, Boolean isCorrect, int answerDuration);
}
