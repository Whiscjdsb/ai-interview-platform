package com.example.aiinterview.module.ai.service.impl;

import java.util.List;
import java.util.Set;

final class AiAnswerQualityEvaluator {

    static final Set<String> GENERIC_REFERENCE_POINTS = Set.of(
            "\u6838\u5fc3\u6982\u5ff5",
            "\u5178\u578b\u573a\u666f",
            "\u5e38\u89c1\u95ee\u9898",
            "\u9002\u7528\u573a\u666f",
            "\u5e94\u7528\u573a\u666f");

    static final List<String> INVALID_PHRASES = List.of(
            "\u6211\u4e0d\u4f1a",
            "\u4e0d\u4f1a",
            "\u4e0d\u77e5\u9053",
            "\u4e0d\u6e05\u695a",
            "\u4e0d\u4e86\u89e3",
            "\u6ca1\u5b66\u8fc7",
            "\u6c92\u5b66\u8fc7",
            "\u6ca1\u6709\u5b66\u8fc7",
            "\u4e0d\u4f1a\u7b54",
            "\u4e0d\u77e5\u9053\u600e\u4e48\u7b54",
            "\u4e0d\u77e5\u9053\u4e86",
            "\u6ca1\u601d\u8def",
            "\u65e0\u4ece\u4e0b\u624b",
            "\u55ef",
            "\u54e6",
            "\u5bf9",
            "\u662f",
            "\u4e0d\u662f",
            "no idea",
            "i don't know",
            "i dont know",
            "not sure");

    static final List<String> TECHNICAL_KEYWORDS = List.of(
            "java", "spring", "springboot", "bean", "ioc", "aop", "jvm", "mysql", "sql", "index",
            "transaction", "redis", "cache", "jwt", "http", "thread", "concurrent", "lock", "starter",
            "classpath", "annotation", "configuration", "database", "queue", "kafka", "rabbitmq",
            "\u6ce8\u89e3", "\u81ea\u52a8\u914d\u7f6e", "\u6761\u4ef6", "\u5bb9\u5668", "\u4e8b\u52a1", "\u7d22\u5f15",
            "\u7f13\u5b58", "\u5e03\u9686", "\u7a7f\u900f", "\u51fb\u7a7f", "\u96ea\u5d29", "\u7ebf\u7a0b",
            "\u5e76\u53d1", "\u9501", "\u96c6\u5408", "\u7c7b\u52a0\u8f7d", "\u5783\u573e\u56de\u6536",
            "\u9650\u6d41", "\u964d\u7ea7", "\u7194\u65ad", "\u5e42\u7b49", "\u6d88\u606f\u961f\u5217",
            "\u67b6\u6784", "\u6e90\u7801", "\u6570\u636e\u5e93", "\u8fde\u63a5\u6c60", "\u4e3b\u4ece",
            "\u5206\u5e03\u5f0f", "\u4e00\u81f4\u6027", "\u5e8f\u5217\u5316");

    static final List<String> EXPLANATION_MARKERS = List.of(
            "because", "when", "for example", "used to", "uses", "relies on", "based on", "so that",
            "\u56e0\u4e3a", "\u6240\u4ee5", "\u4f8b\u5982", "\u573a\u666f", "\u539f\u7406", "\u6d41\u7a0b",
            "\u901a\u8fc7", "\u4f7f\u7528", "\u53ef\u4ee5", "\u89e3\u51b3", "\u5bfc\u81f4", "\u5b9e\u73b0",
            "\u9002\u5408", "\u76ee\u7684", "\u4f5c\u7528", "\u533a\u522b", "\u6b65\u9aa4");

    private AiAnswerQualityEvaluator() {
    }
}
