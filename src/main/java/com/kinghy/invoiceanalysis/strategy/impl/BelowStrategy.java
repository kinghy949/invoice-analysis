package com.kinghy.invoiceanalysis.strategy.impl;

import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.util.TextPositionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 下方提取策略
 * 在关键字下方区域查找字段值
 *
 * 支持的options参数:
 * - maxLinesBelow: Integer - 向下搜索的最大行数，默认1
 * - stopAtKeywords: List<String> - 遇到这些关键字时停止搜索
 * - xAlignment: String - X坐标对齐方式: "LEFT"(与关键字左对齐),"RIGHT"(右对齐),"ANY"(任意位置)，默认"ANY"
 * - xTolerance: Double - X坐标对齐容差，默认50.0
 * - lineHeight: Double - 行高估计值倍数，默认使用关键字高度的2.0倍
 */
@Slf4j
@Component
public class BelowStrategy implements ExtractionStrategy {

    @Override
    public String getStrategyName() {
        return "BELOW";
    }

    @Override
    public String extract(ExtractionContext context) {
        List<TextPosition> allPositions = context.getAllTextPositions();
        List<String> keywords = context.getKeywords();
        Map<String, Object> options = context.getOptions();

        if (keywords == null || keywords.isEmpty()) {
            log.warn("字段 {} 未配置关键字", context.getFieldName());
            return null;
        }

        // 1. 查找关键字位置（使用起始位置用于对齐）
        TextPosition keywordStartPosition = null;
        TextPosition keywordEndPosition = null;
        String matchedKeyword = null;
        
        for (String keyword : keywords) {
            keywordStartPosition = TextPositionUtil.findKeywordStartPosition(allPositions, keyword);
            keywordEndPosition = TextPositionUtil.findKeywordPosition(allPositions, keyword);
            if (keywordStartPosition != null && keywordEndPosition != null) {
                matchedKeyword = keyword;
                log.debug("BELOW策略找到关键字: {}", keyword);
                break;
            }
        }

        if (keywordStartPosition == null) {
            log.warn("字段 {} 未找到任何关键字: {}", context.getFieldName(), keywords);
            return null;
        }

        // 2. 解析options参数
        int maxLinesBelow = TextPositionUtil.getIntOption(options, "maxLinesBelow", 1);
        List<String> stopKeywords = TextPositionUtil.getListOption(options, "stopAtKeywords");
        String xAlignment = TextPositionUtil.getStringOption(options, "xAlignment", "ANY");
        double xTolerance = TextPositionUtil.getDoubleOption(options, "xTolerance", 50.0);
        double lineHeightMultiplier = TextPositionUtil.getDoubleOption(options, "lineHeight", 2.0);

        // 3. 计算搜索区域
        float lineHeight = (float) (keywordEndPosition.getHeight() * lineHeightMultiplier);
        float searchYStart = keywordEndPosition.getY() + keywordEndPosition.getHeight();
        float searchYEnd = searchYStart + (lineHeight * maxLinesBelow);

        // 4. 根据xAlignment确定X范围
        float searchXStart, searchXEnd;
        switch (xAlignment.toUpperCase()) {
            case "LEFT":
                searchXStart = keywordStartPosition.getX() - (float) xTolerance;
                searchXEnd = keywordStartPosition.getX() + (float) xTolerance;
                break;
            case "RIGHT":
                searchXStart = keywordEndPosition.getEndX() - (float) xTolerance;
                searchXEnd = keywordEndPosition.getEndX() + (float) xTolerance;
                break;
            case "ANY":
            default:
                searchXStart = 0;
                searchXEnd = context.getPageWidth();
                break;
        }

        log.debug("BELOW策略搜索区域: Y({}-{}), X({}-{})", searchYStart, searchYEnd, searchXStart, searchXEnd);

        // 5. 收集下方区域的文本
        List<TextPosition> belowPositions = new ArrayList<>();
        for (TextPosition text : allPositions) {
            // 检查Y坐标
            if (text.getY() < searchYStart || text.getY() > searchYEnd) {
                continue;
            }

            // 检查X坐标
            if (text.getX() < searchXStart || text.getX() > searchXEnd) {
                continue;
            }

            belowPositions.add(text);
        }

        if (belowPositions.isEmpty()) {
            log.warn("字段 {} 在关键字 {} 下方未找到文本", context.getFieldName(), matchedKeyword);
            return null;
        }

        // 6. 按位置排序（先按Y，再按X）
        belowPositions.sort(Comparator.comparing(TextPosition::getY)
                .thenComparing(TextPosition::getX));

        // 7. 拼接文本，检查停止关键字
        StringBuilder result = new StringBuilder();
        for (TextPosition tp : belowPositions) {
            String unicode = tp.getUnicode();
            
            // 检查停止关键字
            if (stopKeywords != null && !stopKeywords.isEmpty()) {
                String currentText = result.toString() + unicode;
                boolean shouldStop = false;
                for (String stopKw : stopKeywords) {
                    if (currentText.contains(stopKw)) {
                        shouldStop = true;
                        break;
                    }
                }
                if (shouldStop) {
                    break;
                }
            }
            
            result.append(unicode);
        }

        String value = result.toString().trim();
        log.info("字段 {} BELOW策略提取结果: {}", context.getFieldName(), value);
        return value;
    }

    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null) {
            return true;
        }

        Integer maxLinesBelow = TextPositionUtil.getIntOption(options, "maxLinesBelow", 1);
        if (maxLinesBelow != null && maxLinesBelow <= 0) {
            log.error("BELOW策略参数maxLinesBelow必须大于0");
            return false;
        }

        String xAlignment = TextPositionUtil.getStringOption(options, "xAlignment", "ANY");
        if (xAlignment != null) {
            String upper = xAlignment.toUpperCase();
            if (!"LEFT".equals(upper) && !"RIGHT".equals(upper) && !"ANY".equals(upper)) {
                log.error("BELOW策略参数xAlignment非法: {}", xAlignment);
                return false;
            }
        }

        Double xTolerance = TextPositionUtil.getDoubleOption(options, "xTolerance", 50.0);
        if (xTolerance != null && xTolerance < 0) {
            log.error("BELOW策略参数xTolerance不能小于0");
            return false;
        }

        Double lineHeight = TextPositionUtil.getDoubleOption(options, "lineHeight", 2.0);
        if (lineHeight != null && lineHeight <= 0) {
            log.error("BELOW策略参数lineHeight必须大于0");
            return false;
        }

        List<String> stopKeywords = TextPositionUtil.getListOption(options, "stopAtKeywords");
        if (options.containsKey("stopAtKeywords") && stopKeywords == null) {
            log.error("BELOW策略参数stopAtKeywords必须为字符串数组");
            return false;
        }
        return true;
    }
}
