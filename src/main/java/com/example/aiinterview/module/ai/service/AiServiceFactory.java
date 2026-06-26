package com.example.aiinterview.module.ai.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiServiceFactory {

    private final List<AiService> aiServices;

    @Value("${app.ai.provider:mock}")
    private String provider;

    public AiService current() {
        return aiServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase(provider))
                .findFirst()
                .orElseGet(() -> aiServices.stream()
                        .filter(service -> service.provider().equalsIgnoreCase("mock"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No AI service is available")));
    }
}
