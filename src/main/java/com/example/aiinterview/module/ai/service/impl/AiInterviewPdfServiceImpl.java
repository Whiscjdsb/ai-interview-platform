package com.example.aiinterview.module.ai.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.ai.entity.AiReviewRecord;
import com.example.aiinterview.module.ai.enums.AiRecordType;
import com.example.aiinterview.module.ai.mapper.AiReviewRecordMapper;
import com.example.aiinterview.module.ai.service.AiInterviewPdfService;
import com.example.aiinterview.module.ai.vo.AiInterviewQuestionResultVO;
import com.example.aiinterview.module.ai.vo.AiInterviewSubmitResponseVO;
import com.example.aiinterview.module.user.entity.SysUser;
import com.example.aiinterview.module.user.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiInterviewPdfServiceImpl implements AiInterviewPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiReviewRecordMapper aiReviewRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    @Override
    public byte[] generatePdf(Long userId, Long interviewId) {
        AiReviewRecord record = aiReviewRecordMapper.selectOne(new LambdaQueryWrapper<AiReviewRecord>()
                .eq(AiReviewRecord::getId, interviewId)
                .eq(AiReviewRecord::getUserId, userId)
                .eq(AiReviewRecord::getRecordType, AiRecordType.MOCK_INTERVIEW)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "AI interview record does not exist");
        }
        if (!StringUtils.hasText(record.getResultContent())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI interview result is not ready");
        }

        AiInterviewSubmitResponseVO result = parseResult(record.getResultContent());
        SysUser user = sysUserMapper.selectById(userId);
        return writePdf(record, result, user, parseStructuredResult(record.getResultContent()));
    }

    private AiInterviewSubmitResponseVO parseResult(String resultContent) {
        try {
            JsonNode root = objectMapper.readTree(resultContent);
            if (!root.has("questionResults")) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "AI interview result is not ready");
            }
            return objectMapper.treeToValue(root, AiInterviewSubmitResponseVO.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to parse AI interview result");
        }
    }

    private JsonNode parseStructuredResult(String resultContent) {
        try {
            JsonNode root = objectMapper.readTree(resultContent);
            JsonNode structured = root.path("structuredResult");
            return structured.isMissingNode() || structured.isNull() ? objectMapper.createObjectNode() : structured;
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private byte[] writePdf(
            AiReviewRecord record,
            AiInterviewSubmitResponseVO result,
            SysUser user,
            JsonNode structuredResult) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            FontSet fonts = buildFonts();
            addTitle(document, fonts.title());
            addBasicInfo(document, fonts, record, result, user);
            addScoreTable(document, fonts, result);
            addRadarText(document, fonts, score(result));
            addAnalysis(document, fonts, result, structuredResult);
            addQuestionResults(document, fonts, result.getQuestionResults());

            document.close();
            return outputStream.toByteArray();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate AI interview PDF");
        }
    }

    private void addTitle(Document document, Font titleFont) throws Exception {
        Paragraph title = new Paragraph("AI面试报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18F);
        document.add(title);
    }

    private void addBasicInfo(
            Document document,
            FontSet fonts,
            AiReviewRecord record,
            AiInterviewSubmitResponseVO result,
            SysUser user) throws Exception {
        addSectionTitle(document, "一、基本信息", fonts.section());
        PdfPTable table = table(2);
        addRow(table, "用户", user == null ? "未知用户" : displayUser(user), fonts);
        addRow(table, "面试岗位", result.getPosition(), fonts);
        addRow(table, "面试难度", result.getDifficulty() == null ? "-" : result.getDifficulty().name(), fonts);
        addRow(table, "面试时间", record.getCreateTime() == null ? "-" : DATE_TIME_FORMATTER.format(record.getCreateTime()), fonts);
        addRow(table, "模型", result.getModelName(), fonts);
        document.add(table);
    }

    private void addScoreTable(Document document, FontSet fonts, AiInterviewSubmitResponseVO result) throws Exception {
        addSectionTitle(document, "二、评分概览", fonts.section());
        PdfPTable table = table(2);
        addRow(table, "总分", String.valueOf(score(result)), fonts);
        addRow(table, "等级", result.getLevel(), fonts);
        addRow(table, "总评", result.getSummary(), fonts);
        document.add(table);
    }

    private void addRadarText(Document document, FontSet fonts, int score) throws Exception {
        addSectionTitle(document, "三、能力雷达数据", fonts.section());
        PdfPTable table = table(2);
        addRow(table, "Java基础", String.valueOf(clamp(score + 2)), fonts);
        addRow(table, "Spring Boot", String.valueOf(clamp(score - 3)), fonts);
        addRow(table, "数据库", String.valueOf(clamp(score)), fonts);
        addRow(table, "系统设计", String.valueOf(clamp(score - 7)), fonts);
        addRow(table, "项目经验", String.valueOf(clamp(score - 5)), fonts);
        document.add(table);
    }

    private void addAnalysis(
            Document document,
            FontSet fonts,
            AiInterviewSubmitResponseVO result,
            JsonNode structuredResult) throws Exception {
        addSectionTitle(document, "四、AI分析", fonts.section());
        addListBlock(document, "优点", firstList(structuredResult, "strengths", result.getAdvantages()), fonts);
        addListBlock(document, "不足", firstList(structuredResult, "weaknesses", result.getDisadvantages()), fonts);
        addListBlock(document, "改进建议", firstList(structuredResult, "suggestions", result.getSuggestions()), fonts);
        String referenceAnswer = structuredResult.path("referenceAnswer").asText("");
        if (!StringUtils.hasText(referenceAnswer)) {
            referenceAnswer = firstReferenceAnswer(result.getQuestionResults());
        }
        addTextBlock(document, "AI参考答案", referenceAnswer, fonts);
    }

    private void addQuestionResults(
            Document document,
            FontSet fonts,
            List<AiInterviewQuestionResultVO> questionResults) throws Exception {
        addSectionTitle(document, "五、逐题点评与追问记录", fonts.section());
        if (CollectionUtils.isEmpty(questionResults)) {
            addParagraph(document, "暂无逐题点评。", fonts.normal());
            return;
        }
        for (AiInterviewQuestionResultVO item : questionResults) {
            addParagraph(document, "第 " + item.getQuestionNo() + " 题：" + safe(item.getQuestion()), fonts.bold());
            PdfPTable table = table(2);
            addRow(table, "得分", String.valueOf(item.getScore()), fonts);
            addRow(table, "我的回答/追问记录", item.getUserAnswer(), fonts);
            addRow(table, "AI点评", firstText(item.getReview(), item.getComment()), fonts);
            addRow(table, "参考答案", firstText(item.getReferenceAnswer(), item.getSuggestedAnswer()), fonts);
            document.add(table);
            document.add(Chunk.NEWLINE);
        }
    }

    private void addListBlock(Document document, String title, List<String> items, FontSet fonts) throws Exception {
        addParagraph(document, title, fonts.bold());
        if (CollectionUtils.isEmpty(items)) {
            addParagraph(document, "暂无", fonts.normal());
            return;
        }
        for (String item : items) {
            addParagraph(document, "• " + item, fonts.normal());
        }
    }

    private void addTextBlock(Document document, String title, String content, FontSet fonts) throws Exception {
        addParagraph(document, title, fonts.bold());
        addParagraph(document, StringUtils.hasText(content) ? content : "暂无", fonts.normal());
    }

    private void addSectionTitle(Document document, String title, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(10F);
        paragraph.setSpacingAfter(8F);
        document.add(paragraph);
    }

    private void addParagraph(Document document, String content, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(safe(content), font);
        paragraph.setLeading(18F);
        paragraph.setSpacingAfter(6F);
        document.add(paragraph);
    }

    private PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10F);
        return table;
    }

    private void addRow(PdfPTable table, String label, String value, FontSet fonts) {
        table.addCell(cell(label, fonts.bold(), new Color(245, 247, 250)));
        table.addCell(cell(safe(value), fonts.normal(), Color.WHITE));
    }

    private PdfPCell cell(String text, Font font, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setPadding(8F);
        cell.setBackgroundColor(backgroundColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private FontSet buildFonts() throws Exception {
        BaseFont baseFont = cjkBaseFont();
        return new FontSet(
                new Font(baseFont, 20, Font.BOLD),
                new Font(baseFont, 14, Font.BOLD),
                new Font(baseFont, 11, Font.BOLD),
                new Font(baseFont, 10, Font.NORMAL));
    }

    private BaseFont cjkBaseFont() throws Exception {
        List<String> candidates = List.of(
                "C:/Windows/Fonts/msyh.ttc,0",
                "C:/Windows/Fonts/simsun.ttc,0",
                "/System/Library/Fonts/PingFang.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc,0",
                "/usr/share/fonts/truetype/arphic/uming.ttc,0");
        for (String candidate : candidates) {
            String path = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(',')) : candidate;
            if (Files.exists(Path.of(path))) {
                return BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
    }

    private String displayUser(SysUser user) {
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname() + "（" + user.getUsername() + "）";
        }
        return user.getUsername();
    }

    private int score(AiInterviewSubmitResponseVO result) {
        return result.getTotalScore() == null ? 0 : result.getTotalScore();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private List<String> firstList(JsonNode structuredResult, String field, List<String> fallback) {
        JsonNode node = structuredResult.path(field);
        if (node.isArray() && !node.isEmpty()) {
            return objectMapper.convertValue(node, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }
        return fallback == null ? List.of() : fallback;
    }

    private String firstReferenceAnswer(List<AiInterviewQuestionResultVO> questionResults) {
        if (CollectionUtils.isEmpty(questionResults)) {
            return "";
        }
        return questionResults.stream()
                .map(item -> firstText(item.getReferenceAnswer(), item.getSuggestedAnswer()))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    private String firstText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private record FontSet(Font title, Font section, Font bold, Font normal) {
    }
}
