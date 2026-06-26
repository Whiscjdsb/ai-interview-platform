package com.example.aiinterview.module.statistics.service.impl;

import java.time.LocalDate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.module.statistics.entity.DailyLearningRecord;
import com.example.aiinterview.module.statistics.mapper.DailyLearningRecordMapper;
import com.example.aiinterview.module.statistics.service.DailyLearningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyLearningRecordServiceImpl implements DailyLearningRecordService {

    private final DailyLearningRecordMapper dailyLearningRecordMapper;

    @Override
    public void increaseDailyLearning(Long userId, LocalDate recordDate, Boolean isCorrect, int answerDuration) {
        DailyLearningRecord record = dailyLearningRecordMapper.selectOne(new LambdaQueryWrapper<DailyLearningRecord>()
                .eq(DailyLearningRecord::getUserId, userId)
                .eq(DailyLearningRecord::getRecordDate, recordDate)
                .last("LIMIT 1"));

        int correctIncrement = Boolean.TRUE.equals(isCorrect) ? 1 : 0;
        if (record == null) {
            record = new DailyLearningRecord();
            record.setUserId(userId);
            record.setRecordDate(recordDate);
            record.setAnswerCount(1);
            record.setCorrectCount(correctIncrement);
            record.setStudyDuration(answerDuration);
            record.setDeleted(0);
            dailyLearningRecordMapper.insert(record);
            return;
        }

        record.setAnswerCount(nullToZero(record.getAnswerCount()) + 1);
        record.setCorrectCount(nullToZero(record.getCorrectCount()) + correctIncrement);
        record.setStudyDuration(nullToZero(record.getStudyDuration()) + answerDuration);
        record.setDeleted(0);
        dailyLearningRecordMapper.updateById(record);
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
