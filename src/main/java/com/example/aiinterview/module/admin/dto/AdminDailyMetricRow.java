package com.example.aiinterview.module.admin.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDailyMetricRow {

    private LocalDate metricDate;
    private Long count;
    private Long activeUserCount;
}
