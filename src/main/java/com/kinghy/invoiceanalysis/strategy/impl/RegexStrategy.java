package com.kinghy.invoiceanalysis.strategy.impl;

import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.util.TextPositionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则提取策略
 * 使用正则表达式从全文或指定区域提取字段值
 *
 * 支持的options参数:
 * - pattern: String (必需) - 正则表达式模式
 * - groupIndex: Integer - 捕获组索引，默认0（整个匹配）
 * - searchScope: String - 搜索范围: "FULL"(全文),"AFTER_KEYWORD"(关键字后)，默认"FULL"
 */
@Slf4j
@Component
public class RegexStrategy implements ExtractionStrategy {

    @Override
    public String getStrategyName() {
        return "REGEX";
    }

    @Override
    public String extract(ExtractionContext context) {
        Map<String, Object> options = context.getOptions();

        // 1. 获取必需的pattern参数
        String patternStr = TextPositionUtil.getStringOption(options, "pattern", null);
        if (patternStr == null) {
            log.error("字段 {} REGEX策略缺少pattern参数", context.getFieldName());
            return null;
        }

        // 2. 解析可选参数
        int groupIndex = TextPositionUtil.getIntOption(options, "groupIndex", 0);
        String searchScope = TextPositionUtil.getStringOption(options, "searchScope", "FULL");

        // 3. 确定搜索文本
        String searchText = context.getFullText();
        if ("AFTER_KEYWORD".equalsIgnoreCase(searchScope)) {
            // 查找关键字位置，提取关键字后的文本
            List<String> keywords = context.getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                for (String keyword : keywords) {
                    int index = searchText.indexOf(keyword);
                    if (index >= 0) {
                        searchText = searchText.substring(index + keyword.length());
                        log.debug("REGEX策略：在关键字 {} 后搜索", keyword);
                        break;
                    }
                }
            }
        }

        // 4. 执行正则匹配
        try {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(searchText);

            if (matcher.find()) {
                String value;
                if (groupIndex > 0 && groupIndex <= matcher.groupCount()) {
                    value = matcher.group(groupIndex);
                } else {
                    value = matcher.group();
                }
                log.info("字段 {} REGEX策略提取结果: {}", context.getFieldName(), value);
                return value != null ? value.trim() : null;
            }
        } catch (Exception e) {
            log.error("字段 {} REGEX策略执行失败: {}", context.getFieldName(), e.getMessage(), e);
        }

        log.warn("字段 {} REGEX策略未匹配到内容，模式: {}", context.getFieldName(), patternStr);
        return null;
    }

    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null || !options.containsKey("pattern")) {
            log.error("REGEX策略必须提供pattern参数");
            return false;
        }

        String pattern = TextPositionUtil.getStringOption(options, "pattern", null);
        try {
            Pattern.compile(pattern);
        } catch (Exception e) {
            log.error("REGEX策略pattern无效: {}", pattern);
            return false;
        }

        Integer groupIndex = TextPositionUtil.getIntOption(options, "groupIndex", 0);
        if (groupIndex == null || groupIndex < 0) {
            log.error("REGEX策略参数groupIndex必须大于等于0");
            return false;
        }

        String searchScope = TextPositionUtil.getStringOption(options, "searchScope", "FULL");
        if (searchScope != null) {
            String upper = searchScope.toUpperCase();
            if (!"FULL".equals(upper) && !"AFTER_KEYWORD".equals(upper)) {
                log.error("REGEX策略参数searchScope非法: {}", searchScope);
                return false;
            }
        }
        return true;
    }
}
