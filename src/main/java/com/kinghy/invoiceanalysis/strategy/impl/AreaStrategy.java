package com.kinghy.invoiceanalysis.strategy.impl;

import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.util.TextPositionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 区域提取策略
 * 从指定的矩形区域提取文本
 *
 * 支持的options参数(必须):
 * - x_start: Double - 矩形左上角X坐标
 * - y_start: Double - 矩形左上角Y坐标
 * - width: Double - 矩形宽度
 * - height: Double - 矩形高度
 *
 * 可选参数:
 * - sortOrder: String - 文本排序方式: "X"(按X排序),"Y"(按Y排序),"XY"(先Y后X)，默认"XY"
 */
@Slf4j
@Component
public class AreaStrategy implements ExtractionStrategy {

    @Override
    public String getStrategyName() {
        return "AREA";
    }

    @Override
    public String extract(ExtractionContext context) {
        List<TextPosition> allPositions = context.getAllTextPositions();
        Map<String, Object> options = context.getOptions();

        // 1. 解析必需参数
        Double xStart = TextPositionUtil.getDoubleOption(options, "x_start", null);
        Double yStart = TextPositionUtil.getDoubleOption(options, "y_start", null);
        Double width = TextPositionUtil.getDoubleOption(options, "width", null);
        Double height = TextPositionUtil.getDoubleOption(options, "height", null);

        if (xStart == null || yStart == null || width == null || height == null) {
            log.error("字段 {} AREA策略缺少必需参数: x_start, y_start, width, height", context.getFieldName());
            return null;
        }

        // 2. 计算区域边界
        float xEnd = xStart.floatValue() + width.floatValue();
        float yEnd = yStart.floatValue() + height.floatValue();

        log.debug("AREA策略搜索区域: ({}, {}) - ({}, {})", xStart, yStart, xEnd, yEnd);

        // 3. 收集区域内的文本
        List<TextPosition> areaPositions = new ArrayList<>();
        for (TextPosition text : allPositions) {
            float x = text.getX();
            float y = text.getY();

            if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                areaPositions.add(text);
            }
        }

        if (areaPositions.isEmpty()) {
            log.warn("字段 {} 在指定区域内未找到文本", context.getFieldName());
            return null;
        }

        // 4. 根据sortOrder排序
        String sortOrder = TextPositionUtil.getStringOption(options, "sortOrder", "XY");
        switch (sortOrder.toUpperCase()) {
            case "X":
                areaPositions.sort(Comparator.comparing(TextPosition::getX));
                break;
            case "Y":
                areaPositions.sort(Comparator.comparing(TextPosition::getY));
                break;
            case "XY":
            default:
                areaPositions.sort(Comparator.comparing(TextPosition::getY)
                        .thenComparing(TextPosition::getX));
                break;
        }

        // 5. 拼接文本
        StringBuilder result = new StringBuilder();
        for (TextPosition tp : areaPositions) {
            result.append(tp.getUnicode());
        }

        String value = result.toString().trim();
        log.info("字段 {} AREA策略提取结果: {}", context.getFieldName(), value);
        return value;
    }

    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null) {
            log.error("AREA策略必须提供options参数");
            return false;
        }

        String[] requiredKeys = {"x_start", "y_start", "width", "height"};
        for (String key : requiredKeys) {
            if (!options.containsKey(key)) {
                log.error("AREA策略缺少必需参数: {}", key);
                return false;
            }
        }
        return true;
    }
}
