package com.example.aiinterview.module.ai.service;

import jakarta.annotation.PostConstruct;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiServiceFactory {

    private final List<AiService> aiServices;
    private final Environment environment;

    private String resolvedProvider;
    private String resolvedProviderSource;

    @PostConstruct
    public void logResolvedProviderAtStartup() {
        ProviderConfig config = resolveProviderConfig();
        this.resolvedProvider = config.provider();
        this.resolvedProviderSource = config.source();
        log.info("System final AI Provider = {}, source={}", resolvedProvider, resolvedProviderSource);
        log.debug("Resolved AI provider detail: provider={}, source={}", resolvedProvider, resolvedProviderSource);
    }

    public AiService current() {
        ProviderConfig config = resolveProviderConfig();
        this.resolvedProvider = config.provider();
        this.resolvedProviderSource = config.source();
        AiService configuredService = aiServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase(resolvedProvider))
                .findFirst()
                .orElse(null);
        if (configuredService != null && configuredService.isAvailable()) {
            log.info("AI provider configured={}, source={}, selectedService={}, modelName={}",
                    resolvedProvider,
                    resolvedProviderSource,
                    configuredService.getClass().getSimpleName(),
                    configuredService.modelName());
            return configuredService;
        }
        if (configuredService != null) {
            log.warn("AI provider configured={} source={} selectedService={} is unavailable, fallback to MockAiService",
                    resolvedProvider,
                    resolvedProviderSource,
                    configuredService.getClass().getSimpleName());
        } else {
            log.warn("AI provider configured={} source={} has no matching AiService, fallback to MockAiService",
                    resolvedProvider,
                    resolvedProviderSource);
        }
        AiService mockService = aiServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase("mock"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AI service is available"));
        log.info("AI provider configured={}, source={}, selectedService={}, modelName={}",
                resolvedProvider,
                resolvedProviderSource,
                mockService.getClass().getSimpleName(),
                mockService.modelName());
        return mockService;
    }

    private ProviderConfig resolveProviderConfig() {
        String appProvider = trimToNull(environment.getProperty("app.ai.provider"));
        if (appProvider != null) {
            return new ProviderConfig(appProvider, providerSource(appProvider));
        }
        String appEnvProvider = trimToNull(System.getenv("APP_AI_PROVIDER"));
        if (appEnvProvider != null) {
            return new ProviderConfig(appEnvProvider, "env:APP_AI_PROVIDER");
        }
        String aiEnvProvider = trimToNull(System.getenv("AI_PROVIDER"));
        if (aiEnvProvider != null) {
            return new ProviderConfig(aiEnvProvider, "env:AI_PROVIDER");
        }
        return new ProviderConfig("mock", "default:mock");
    }

    private String providerSource(String provider) {
        String systemProperty = trimToNull(System.getProperty("app.ai.provider"));
        if (provider.equalsIgnoreCase(systemProperty)) {
            return "system-property:app.ai.provider";
        }
        String appEnvProvider = trimToNull(System.getenv("APP_AI_PROVIDER"));
        if (provider.equalsIgnoreCase(appEnvProvider)) {
            return "env:APP_AI_PROVIDER";
        }
        String aiEnvProvider = trimToNull(System.getenv("AI_PROVIDER"));
        if (provider.equalsIgnoreCase(aiEnvProvider)) {
            return "env:AI_PROVIDER";
        }
        return "property:app.ai.provider";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private record ProviderConfig(String provider, String source) {
    }
}
