package com.example.aiinterview.module.ai.service.impl;

import org.springframework.stereotype.Component;

@Component
public class DeepSeekAiService extends MockAiService {

    @Override
    public String provider() {
        return "deepseek";
    }
}
