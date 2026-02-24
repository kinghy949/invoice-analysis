package com.kinghy.invoiceanalysis.strategy.impl;

import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.util.TextPositionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格提取策略
 * 从表格结构中提取数据（基于行列定位）
 *
 * 支持的options参数:
 * - columnIndex: Integer - 目标列索引（从0开始）
 * - rowIndex: Integer - 目标行索引（从0开始，不含表头），如果不指定则提取整列
 * - headerKeyword: String - 表头关键字，用于定位列（与columnIndex二选一）
 * - tableStartY: Double - 表格起始Y坐标
 * - tableEndY: Double - 表格结束Y坐标
 * - rowTolerance: Double - 行分组的Y坐标容差，默认5.0
 * - columnDelimiter: String - 列分隔符，用于分隔结果，默认","
 */
@Slf4j
@Component
public class TableStrategy implements ExtractionStrategy {

    @Override
    public String getStrategyName() {
        return "TABLE";
    }

    @Override
    public String extract(ExtractionContext context) {
        Map<String, Object> options = context.getOptions();
        List<TextPosition> allPositions = context.getAllTextPositions();

        // 1. 解析参数
        Integer columnIndex = TextPositionUtil.getIntOption(options, "columnIndex", null);
        Integer rowIndex = TextPositionUtil.getIntOption(options, "rowIndex", null);
        String headerKeyword = TextPositionUtil.getStringOption(options, "headerKeyword", null);
        Double tableStartY = TextPositionUtil.getDoubleOption(options, "tableStartY", null);
        Double tableEndY = TextPositionUtil.getDoubleOption(options, "tableEndY", null);
        double rowTolerance = TextPositionUtil.getDoubleOption(options, "rowTolerance", 5.0);
        String columnDelimiter = TextPositionUtil.getStringOption(options, "columnDelimiter", ",");

        if (columnIndex == null && headerKeyword == null) {
            log.error("字段 {} TABLE策略必须提供columnIndex或headerKeyword", context.getFieldName());
            return null;
        }

        // 2. 过滤表格区域的文本
        List<TextPosition> tablePositions = allPositions;
        if (tableStartY != null || tableEndY != null) {
            double startY = tableStartY != null ? tableStartY : 0;
            double endY = tableEndY != null ? tableEndY : context.getPageHeight();
            tablePositions = allPositions.stream()
                    .filter(tp -> tp.getY() >= startY && tp.getY() <= endY)
                    .collect(Collectors.toList());
        }

        if (tablePositions.isEmpty()) {
            log.warn("字段 {} 在表格区域内未找到文本", context.getFieldName());
            return null;
        }

        // 3. 按行分组（Y坐标相近的为同一行）
        List<List<TextPosition>> rows = groupByRows(tablePositions, rowTolerance);
        log.debug("TABLE策略识别到 {} 行", rows.size());

        if (rows.isEmpty()) {
            return null;
        }

        // 4. 如果提供headerKeyword，查找列索引
        if (headerKeyword != null && columnIndex == null) {
            columnIndex = findColumnByHeader(rows, headerKeyword);
            if (columnIndex == null) {
                log.warn("字段 {} 未找到表头关键字: {}", context.getFieldName(), headerKeyword);
                return null;
            }
            log.debug("通过表头关键字 {} 定位到列索引: {}", headerKeyword, columnIndex);
        }

        // 5. 计算列边界
        List<Float> columnBoundaries = calculateColumnBoundaries(rows);
        log.debug("TABLE策略识别到列边界: {}", columnBoundaries);

        // 6. 提取单元格或整列
        String value;
        if (rowIndex != null) {
            // 提取单个单元格（跳过表头行）
            int dataRowIndex = rowIndex + 1;
            if (dataRowIndex < rows.size()) {
                value = extractCellValue(rows.get(dataRowIndex), columnIndex, columnBoundaries);
            } else {
                log.warn("字段 {} 请求的行索引 {} 超出范围（共 {} 行数据）", 
                        context.getFieldName(), rowIndex, rows.size() - 1);
                return null;
            }
        } else {
            // 提取整列（跳过表头）
            List<String> columnValues = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String cellValue = extractCellValue(rows.get(i), columnIndex, columnBoundaries);
                if (cellValue != null && !cellValue.isEmpty()) {
                    columnValues.add(cellValue);
                }
            }
            value = String.join(columnDelimiter, columnValues);
        }

        log.info("字段 {} TABLE策略提取结果: {}", context.getFieldName(), value);
        return value;
    }

    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null) {
            return false;
        }

        boolean hasColumnIndex = options.containsKey("columnIndex");
        boolean hasHeaderKeyword = options.containsKey("headerKeyword");

        if (!hasColumnIndex && !hasHeaderKeyword) {
            log.error("TABLE策略必须提供columnIndex或headerKeyword");
            return false;
        }

        Integer columnIndex = TextPositionUtil.getIntOption(options, "columnIndex", null);
        if (hasColumnIndex && (columnIndex == null || columnIndex < 0)) {
            log.error("TABLE策略参数columnIndex必须为大于等于0的整数");
            return false;
        }

        Integer rowIndex = TextPositionUtil.getIntOption(options, "rowIndex", null);
        if (options.containsKey("rowIndex") && (rowIndex == null || rowIndex < 0)) {
            log.error("TABLE策略参数rowIndex必须为大于等于0的整数");
            return false;
        }

        String headerKeyword = TextPositionUtil.getStringOption(options, "headerKeyword", null);
        if (hasHeaderKeyword && (headerKeyword == null || headerKeyword.trim().isEmpty())) {
            log.error("TABLE策略参数headerKeyword不能为空");
            return false;
        }

        Double tableStartY = TextPositionUtil.getDoubleOption(options, "tableStartY", null);
        Double tableEndY = TextPositionUtil.getDoubleOption(options, "tableEndY", null);
        if (tableStartY != null && tableEndY != null && tableStartY > tableEndY) {
            log.error("TABLE策略参数tableStartY不能大于tableEndY");
            return false;
        }

        Double rowTolerance = TextPositionUtil.getDoubleOption(options, "rowTolerance", 5.0);
        if (rowTolerance != null && rowTolerance <= 0) {
            log.error("TABLE策略参数rowTolerance必须大于0");
            return false;
        }

        String columnDelimiter = TextPositionUtil.getStringOption(options, "columnDelimiter", ",");
        if (options.containsKey("columnDelimiter") && columnDelimiter == null) {
            log.error("TABLE策略参数columnDelimiter必须为字符串");
            return false;
        }
        return true;
    }

    /**
     * 按行分组（Y坐标容差内的为同一行）
     */
    private List<List<TextPosition>> groupByRows(List<TextPosition> positions, double rowTolerance) {
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }

        // 按Y坐标排序
        List<TextPosition> sorted = new ArrayList<>(positions);
        sorted.sort(Comparator.comparing(TextPosition::getY));

        List<List<TextPosition>> rows = new ArrayList<>();
        List<TextPosition> currentRow = new ArrayList<>();
        float lastY = sorted.get(0).getY();

        for (TextPosition tp : sorted) {
            if (Math.abs(tp.getY() - lastY) > rowTolerance) {
                // 新行
                if (!currentRow.isEmpty()) {
                    // 按X坐标排序当前行
                    currentRow.sort(Comparator.comparing(TextPosition::getX));
                    rows.add(currentRow);
                }
                currentRow = new ArrayList<>();
                lastY = tp.getY();
            }
            currentRow.add(tp);
        }

        // 添加最后一行
        if (!currentRow.isEmpty()) {
            currentRow.sort(Comparator.comparing(TextPosition::getX));
            rows.add(currentRow);
        }

        return rows;
    }

    /**
     * 根据表头关键字查找列索引
     */
    private Integer findColumnByHeader(List<List<TextPosition>> rows, String headerKeyword) {
        if (rows.isEmpty()) {
            return null;
        }

        // 假设第一行是表头
        List<TextPosition> headerRow = rows.get(0);
        StringBuilder headerText = new StringBuilder();
        
        for (TextPosition tp : headerRow) {
            headerText.append(tp.getUnicode());
        }

        // 在表头文本中查找关键字位置
        String header = headerText.toString();
        int keywordIndex = header.indexOf(headerKeyword);
        if (keywordIndex < 0) {
            return null;
        }

        // 计算关键字所在的列（简化处理：按字符索引估算列）
        // 更精确的方法是计算关键字的X坐标并与列边界比较
        List<Float> columnBoundaries = calculateColumnBoundaries(rows);
        
        // 找到关键字对应的TextPosition
        int charCount = 0;
        for (int i = 0; i < headerRow.size(); i++) {
            charCount += headerRow.get(i).getUnicode().length();
            if (charCount > keywordIndex) {
                // 找到关键字所在的TextPosition，确定其列索引
                float x = headerRow.get(i).getX();
                for (int col = 0; col < columnBoundaries.size() - 1; col++) {
                    if (x >= columnBoundaries.get(col) && x < columnBoundaries.get(col + 1)) {
                        return col;
                    }
                }
                break;
            }
        }

        return 0;
    }

    /**
     * 计算列边界（基于所有行的X坐标聚类）
     */
    private List<Float> calculateColumnBoundaries(List<List<TextPosition>> rows) {
        // 收集所有X坐标
        List<Float> allXPositions = new ArrayList<>();
        for (List<TextPosition> row : rows) {
            for (TextPosition tp : row) {
                allXPositions.add(tp.getX());
            }
        }
        
        if (allXPositions.isEmpty()) {
            return Arrays.asList(0f, Float.MAX_VALUE);
        }

        Collections.sort(allXPositions);

        // 简单的列边界检测：查找X坐标的间隙
        List<Float> boundaries = new ArrayList<>();
        boundaries.add(0f);

        float lastX = allXPositions.get(0);
        float gapThreshold = 20f; // 列间隙阈值

        for (Float x : allXPositions) {
            if (x - lastX > gapThreshold) {
                boundaries.add((lastX + x) / 2);
            }
            lastX = x;
        }

        boundaries.add(Float.MAX_VALUE);
        return boundaries;
    }

    /**
     * 提取指定列的单元格值
     */
    private String extractCellValue(List<TextPosition> row, int columnIndex, List<Float> columnBoundaries) {
        if (columnIndex < 0 || columnIndex >= columnBoundaries.size() - 1) {
            return null;
        }

        float colStart = columnBoundaries.get(columnIndex);
        float colEnd = columnBoundaries.get(columnIndex + 1);

        StringBuilder cellText = new StringBuilder();
        for (TextPosition tp : row) {
            if (tp.getX() >= colStart && tp.getX() < colEnd) {
                cellText.append(tp.getUnicode());
            }
        }

        return cellText.toString().trim();
    }
}
