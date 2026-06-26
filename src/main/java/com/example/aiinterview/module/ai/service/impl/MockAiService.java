package com.example.aiinterview.module.ai.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.aiinterview.module.ai.dto.GenerateInterviewRequest;
import com.example.aiinterview.module.ai.service.AiService;
import com.example.aiinterview.module.ai.vo.AiAnswerReviewVO;
import com.example.aiinterview.module.ai.vo.GenerateInterviewVO;
import com.example.aiinterview.module.ai.vo.MockInterviewQuestionVO;
import com.example.aiinterview.module.ai.vo.WeaknessItemVO;
import com.example.aiinterview.module.ai.vo.WeaknessSummaryVO;
import com.example.aiinterview.module.question.entity.Question;
import com.example.aiinterview.module.question.enums.QuestionDifficulty;
import com.example.aiinterview.module.question.vo.TagVO;
import com.example.aiinterview.module.statistics.vo.TagAccuracyVO;
import com.example.aiinterview.module.statistics.vo.WrongAnalysisVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MockAiService implements AiService {

    private static final String MODEL_NAME = "MOCK";

    private static final Map<String, List<String>> KEYWORDS = Map.of(
            "redis", List.of("缓存", "cache", "redis", "穿透", "击穿", "雪崩", "数据结构"),
            "mysql", List.of("索引", "index", "事务", "隔离", "锁", "执行计划"),
            "spring", List.of("spring", "bean", "自动配置", "注解", "ioc", "aop"),
            "java", List.of("jvm", "集合", "线程", "并发", "string", "泛型"));

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public String modelName() {
        return MODEL_NAME;
    }

    @Override
    public AiAnswerReviewVO reviewAnswer(Question question, List<TagVO> tags, String answer) {
        String normalized = answer.toLowerCase(Locale.ROOT);
        int score = 45;

        // Mock scoring favors sufficient length, domain keywords, and tag-aware coverage.
        if (answer.length() >= 80) {
            score += 15;
        } else if (answer.length() >= 40) {
            score += 8;
        }
        int keywordHits = keywordHits(normalized, tags);
        score += Math.min(keywordHits * 6, 24);
        if (StringUtils.hasText(question.getCorrectAnswer())) {
            score += containsAny(normalized, question.getCorrectAnswer().toLowerCase(Locale.ROOT).split("[,，。；;\\s]+")) ? 10 : 0;
        }
        score = Math.max(0, Math.min(100, score));

        List<String> advantages = new ArrayList<>();
        advantages.add("能够围绕题目给出直接回答");
        if (keywordHits > 0) {
            advantages.add("回答中覆盖了部分关键术语");
        }
        if (answer.length() >= 80) {
            advantages.add("回答篇幅较充分，有一定展开");
        }

        List<String> improvements = new ArrayList<>();
        if (keywordHits < 2) {
            improvements.add("补充更多与题目标签相关的核心概念");
        }
        improvements.add("结合应用场景、底层原理和常见问题组织答案");
        improvements.add("给出更清晰的分点结构，便于面试表达");

        return new AiAnswerReviewVO(
                score,
                score >= 80 ? "整体回答较完整，已经覆盖主要方向。" : "整体回答方向基本正确，但关键细节仍可继续补充。",
                advantages,
                improvements,
                "建议从概念、应用场景、底层原理和常见问题四个方面组织回答。",
                MODEL_NAME);
    }

    @Override
    public GenerateInterviewVO generateInterview(GenerateInterviewRequest request, List<Question> seedQuestions) {
        List<MockInterviewQuestionVO> questions = new ArrayList<>();
        for (Question seed : seedQuestions) {
            if (questions.size() >= request.getQuestionCount()) {
                break;
            }
            questions.add(new MockInterviewQuestionVO(
                    questions.size() + 1,
                    seed.getContent(),
                    category(seed),
                    seed.getDifficulty(),
                    referencePoints(seed)));
        }

        List<String> generic = List.of(
                "请解释 Spring Boot 自动配置的核心原理。",
                "MySQL 索引失效的常见场景有哪些？",
                "Redis 缓存穿透、击穿和雪崩分别是什么？",
                "Java 线程池的核心参数如何理解？",
                "如何设计一个高并发接口的限流和降级方案？",
                "JVM 垃圾回收的基本流程是什么？",
                "事务隔离级别分别解决哪些问题？",
                "如何定位一个线上接口响应变慢的问题？",
                "Spring Bean 的生命周期包括哪些关键阶段？",
                "消息队列如何保证消费幂等？");
        for (String item : generic) {
            if (questions.size() >= request.getQuestionCount()) {
                break;
            }
            questions.add(new MockInterviewQuestionVO(
                    questions.size() + 1,
                    item,
                    inferCategory(item),
                    request.getDifficulty(),
                    List.of("核心概念", "典型场景", "常见坑点")));
        }

        return new GenerateInterviewVO(request.getPosition(), request.getDifficulty(), questions, MODEL_NAME);
    }

    @Override
    public WeaknessSummaryVO summarizeWeakness(List<TagAccuracyVO> tagAccuracy, List<WrongAnalysisVO> wrongAnalysis) {
        if (tagAccuracy.isEmpty() && wrongAnalysis.isEmpty()) {
            return new WeaknessSummaryVO(
                    "当前还没有足够的答题或错题数据，建议先完成一组题目练习后再生成薄弱点总结。",
                    List.of(),
                    List.of("先完成 5-10 道基础题", "提交答案后查看错题本", "积累数据后再次生成总结"),
                    MODEL_NAME);
        }

        List<String> weakTags = wrongAnalysis.stream()
                .map(WrongAnalysisVO::getTagName)
                .limit(5)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        tagAccuracy.stream()
                .sorted(Comparator.comparing(TagAccuracyVO::getAccuracyRate))
                .map(TagAccuracyVO::getTagName)
                .filter(tag -> !weakTags.contains(tag))
                .limit(Math.max(0, 5 - weakTags.size()))
                .forEach(weakTags::add);

        List<WeaknessItemVO> weaknesses = weakTags.stream()
                .limit(5)
                .map(tag -> new WeaknessItemVO(
                        tag,
                        "该方向错题或低正确率记录较明显，需要优先复习。",
                        List.of("复习 " + tag + " 的核心概念", "完成 " + tag + " 专项练习", "整理错题并二次作答")))
                .toList();
        String summary = "你当前主要薄弱点集中在 " + String.join("、", weakTags.stream().limit(3).toList()) + "。";
        return new WeaknessSummaryVO(
                summary,
                weaknesses,
                List.of("第 1 天：复习最高频错题标签", "第 2 天：完成专项练习", "第 3 天：回顾错题并重新作答"),
                MODEL_NAME);
    }

    private int keywordHits(String answer, List<TagVO> tags) {
        int hits = 0;
        for (TagVO tag : tags) {
            String tagName = tag.getTagName().toLowerCase(Locale.ROOT);
            for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
                if (tagName.contains(entry.getKey())) {
                    for (String keyword : entry.getValue()) {
                        if (answer.contains(keyword.toLowerCase(Locale.ROOT))) {
                            hits++;
                        }
                    }
                }
            }
        }
        return hits;
    }

    private boolean containsAny(String text, String[] terms) {
        for (String term : terms) {
            if (StringUtils.hasText(term) && text.contains(term.trim())) {
                return true;
            }
        }
        return false;
    }

    private String category(Question seed) {
        return seed.getQuestionType() == null ? "Backend" : seed.getQuestionType().name();
    }

    private List<String> referencePoints(Question seed) {
        if (StringUtils.hasText(seed.getAnalysis())) {
            return List.of("核心概念", "适用场景", seed.getAnalysis());
        }
        return List.of("核心概念", "适用场景", "常见问题");
    }

    private String inferCategory(String question) {
        String lower = question.toLowerCase(Locale.ROOT);
        if (lower.contains("redis")) {
            return "Redis";
        }
        if (lower.contains("mysql") || lower.contains("事务")) {
            return "MySQL";
        }
        if (lower.contains("spring")) {
            return "Spring Boot";
        }
        if (lower.contains("jvm") || lower.contains("java")) {
            return "Java";
        }
        return "Backend";
    }
}
