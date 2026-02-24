package com.kinghy.invoiceanalysis.strategy.impl;

import com.kinghy.invoiceanalysis.entity.dto.FieldDefinition;
import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.util.TextPositionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 同行提取策略
 * 在关键字右侧同一行查找字段值
 *
 * 支持的options参数:
 * - trimChars: String - 需要从值开头移除的字符（如": "）
 * - valuePattern: String - 值必须匹配的正则表达式（如"[0-9.]+"）
 * - maxDistance: Double - 关键字和值之间的最大X坐标距离，默认无限制
 * - yTolerance: Double - Y坐标容差因子，默认0.5（即半个字符高度）
 */
@Slf4j
@Component
public class SameLineStrategy implements ExtractionStrategy {

    @Override
    public String getStrategyName() {
        return "SAME_LINE";
    }

    @Override
    public String extract(ExtractionContext context) {
        List<TextPosition> allPositions = context.getAllTextPositions();
        List<String> keywords = context.getKeywords();
        Map<String, Object> options = context.getOptions();
        List<FieldDefinition> templateFields = context.getTemplateFields();

        if (keywords == null || keywords.isEmpty()) {
            log.warn("字段 {} 未配置关键字", context.getFieldName());
            return null;
        }

        // 1. 遍历所有关键字，找到第一个匹配的
        Integer keywordEndIndex = null;
        TextPosition keywordPosition = null;
        String matchedKeyword = null;
        for (String keyword : keywords) {
            keywordEndIndex = findKeywordEndIndex(allPositions, keyword);
            if (keywordEndIndex != null) {
                keywordPosition = allPositions.get(keywordEndIndex);
                matchedKeyword = keyword;
                log.debug("在字段 {} 中找到关键字: {}", context.getFieldName(), keyword);
                break;
            }
        }

        if (keywordPosition == null || keywordEndIndex == null) {
            log.warn("字段 {} 未找到任何关键字: {}", context.getFieldName(), keywords);
            return null;
        }

        // 2. 解析options参数
        double yToleranceFactor = TextPositionUtil.getDoubleOption(options, "yTolerance", 0.5);
        Double maxDistance = TextPositionUtil.getDoubleOption(options, "maxDistance", null);

        // 3. 定义ROI区域
        float yTolerance = (float) (keywordPosition.getHeight() * yToleranceFactor);
        float roiYStart = keywordPosition.getY() - yTolerance;
        float roiYEnd = keywordPosition.getY() + keywordPosition.getHeight() + yTolerance;
        float roiXStart = keywordPosition.getEndX();
        Integer nextKeywordStartIndex = findNextKeywordStartIndex(
                allPositions, keywordEndIndex + 1, roiYStart, roiYEnd, keywords, templateFields
        );

        // 4. 收集ROI内的字符
        List<TextPosition> valuePositions = new ArrayList<>();
        for (int i = keywordEndIndex + 1; i < allPositions.size(); i++) {
            if (nextKeywordStartIndex != null && i >= nextKeywordStartIndex) {
                break;
            }
            TextPosition text = allPositions.get(i);
            if (text.getY() >= roiYStart && text.getY() <= roiYEnd) {
                // 检查距离限制
                if (maxDistance != null && text.getX() - roiXStart > maxDistance) {
                    continue;
                }
                valuePositions.add(text);
            }
        }

        if (valuePositions.isEmpty()) {
            log.warn("字段 {} 在关键字 {} 右侧未找到任何文本", context.getFieldName(), matchedKeyword);
            return null;
        }

        // 5. 按X坐标排序并拼接
        valuePositions.sort(Comparator.comparing(TextPosition::getX));
        StringBuilder result = new StringBuilder();
        for (TextPosition tp : valuePositions) {
            result.append(tp.getUnicode());
        }

        String value = result.toString();

        // 6. 应用trimChars
        String trimChars = TextPositionUtil.getStringOption(options, "trimChars", null);
        if (trimChars != null) {
            value = TextPositionUtil.trimStart(value, trimChars);
        }
        value = value.trim();
        if (value.isEmpty()) {
            log.info("字段 {} 在关键字 {} 与下一关键字之间无有效内容，返回空值", context.getFieldName(), matchedKeyword);
            return null;
        }

        // 7. 应用valuePattern验证
        String valuePattern = TextPositionUtil.getStringOption(options, "valuePattern", null);
        if (valuePattern != null) {
            value = extractByPattern(value, valuePattern);
            if (value == null || value.trim().isEmpty()) {
                log.info("字段 {} 未匹配valuePattern，返回空值", context.getFieldName());
                return null;
            }
        }

        log.info("字段 {} SAME_LINE策略提取结果: {}", context.getFieldName(), value);
        return value;
    }

    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null) {
            return true;
        }

        // 验证valuePattern是否为有效正则表达式
        String pattern = TextPositionUtil.getStringOption(options, "valuePattern", null);
        if (pattern != null) {
            try {
                Pattern.compile(pattern);
            } catch (Exception e) {
                log.error("valuePattern无效: {}", pattern);
                return false;
            }
        }
        return true;
    }

    /**
     * 使用正则表达式提取匹配的部分
     */
    private String extractByPattern(String text, String patternStr) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            log.error("正则表达式匹配失败: {}", patternStr, e);
        }
        return null;
    }

    /**
     * 在TextPosition序列中查找关键字结束索引
     */
    private Integer findKeywordEndIndex(List<TextPosition> allTextPositions, String keyword) {
        if (keyword == null || keyword.isEmpty() || allTextPositions == null || allTextPositions.isEmpty()) {
            return null;
        }

        for (int i = 0; i <= allTextPositions.size() - keyword.length(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < keyword.length(); j++) {
                sb.append(allTextPositions.get(i + j).getUnicode());
            }
            if (keyword.equals(sb.toString())) {
                return i + keyword.length() - 1;
            }
        }
        return null;
    }

    /**
     * 查找同一行中紧随当前字段后的下一个关键字起始索引，用于截断串值
     */
    private Integer findNextKeywordStartIndex(
            List<TextPosition> allTextPositions,
            int searchFromIndex,
            float roiYStart,
            float roiYEnd,
            List<String> currentFieldKeywords,
            List<FieldDefinition> templateFields
    ) {
        if (allTextPositions == null || allTextPositions.isEmpty() || templateFields == null || templateFields.isEmpty()) {
            return null;
        }

        Set<String> stopKeywords = new LinkedHashSet<>();
        for (FieldDefinition field : templateFields) {
            if (field == null || field.getKeywords() == null || field.getKeywords().isEmpty()) {
                continue;
            }
            for (String keyword : field.getKeywords()) {
                if (keyword == null || keyword.isEmpty()) {
                    continue;
                }
                if (currentFieldKeywords != null && currentFieldKeywords.contains(keyword)) {
                    continue;
                }
                stopKeywords.add(keyword);
            }
        }

        if (stopKeywords.isEmpty()) {
            return null;
        }

        for (int i = searchFromIndex; i < allTextPositions.size(); i++) {
            TextPosition tp = allTextPositions.get(i);
            if (tp.getY() < roiYStart || tp.getY() > roiYEnd) {
                continue;
            }

            for (String keyword : stopKeywords) {
                if (matchesKeywordAt(allTextPositions, i, keyword, roiYStart, roiYEnd)) {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * 判断在指定索引处是否命中某个关键字，且关键字字符都位于同一ROI行范围内
     */
    private boolean matchesKeywordAt(
            List<TextPosition> allTextPositions,
            int startIndex,
            String keyword,
            float roiYStart,
            float roiYEnd
    ) {
        if (startIndex < 0 || keyword == null || keyword.isEmpty()) {
            return false;
        }
        if (startIndex + keyword.length() > allTextPositions.size()) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < keyword.length(); j++) {
            TextPosition tp = allTextPositions.get(startIndex + j);
            if (tp.getY() < roiYStart || tp.getY() > roiYEnd) {
                return false;
            }
            sb.append(tp.getUnicode());
        }
        return keyword.equals(sb.toString());
    }
}
