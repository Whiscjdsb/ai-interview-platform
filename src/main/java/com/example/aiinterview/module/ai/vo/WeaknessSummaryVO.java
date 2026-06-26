package com.example.aiinterview.module.ai.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeaknessSummaryVO {

    private String summary;

    private List<WeaknessItemVO> weaknesses;

    private List<String> studyPlan;

    private String modelName;
}
