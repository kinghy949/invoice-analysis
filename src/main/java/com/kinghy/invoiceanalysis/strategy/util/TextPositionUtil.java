package com.kinghy.invoiceanalysis.strategy.util;

import org.apache.pdfbox.text.TextPosition;

import java.util.List;
import java.util.Map;

/**
 * TextPosition工具类
 * 提供关键字查找、位置计算等通用功能
 */
public class TextPositionUtil {

    /**
     * 在文本列表中查找关键字位置（滑动窗口匹配）
     * @param allTextPositions 所有文本位置
     * @param keyword 关键字
     * @return 关键字最后一个字符的TextPosition，未找到返回null
     */
    public static TextPosition findKeywordPosition(List<TextPosition> allTextPositions, String keyword) {
        if (keyword == null || keyword.isEmpty() || allTextPositions == null || allTextPositions.isEmpty()) {
            return null;
        }

        for (int i = 0; i <= allTextPositions.size() - keyword.length(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < keyword.length(); j++) {
                sb.append(allTextPositions.get(i + j).getUnicode());
            }

            if (keyword.equals(sb.toString())) {
                // 返回关键字最后一个字符的位置信息
                return allTextPositions.get(i + keyword.length() - 1);
            }
        }
        return null;
    }

    /**
     * 在文本列表中查找关键字的起始位置
     * @param allTextPositions 所有文本位置
     * @param keyword 关键字
     * @return 关键字第一个字符的TextPosition，未找到返回null
     */
    public static TextPosition findKeywordStartPosition(List<TextPosition> allTextPositions, String keyword) {
        if (keyword == null || keyword.isEmpty() || allTextPositions == null || allTextPositions.isEmpty()) {
            return null;
        }

        for (int i = 0; i <= allTextPositions.size() - keyword.length(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < keyword.length(); j++) {
                sb.append(allTextPositions.get(i + j).getUnicode());
            }

            if (keyword.equals(sb.toString())) {
                // 返回关键字第一个字符的位置信息
                return allTextPositions.get(i);
            }
        }
        return null;
    }

    /**
     * 判断两个TextPosition是否在同一行（Y坐标相近）
     */
    public static boolean isSameLine(TextPosition tp1, TextPosition tp2, float tolerance) {
        return Math.abs(tp1.getY() - tp2.getY()) <= tolerance;
    }

    /**
     * 计算两个TextPosition之间的水平距离
     */
    public static float getHorizontalDistance(TextPosition tp1, TextPosition tp2) {
        return Math.abs(tp1.getX() - tp2.getX());
    }

    /**
     * 计算两个TextPosition之间的垂直距离
     */
    public static float getVerticalDistance(TextPosition tp1, TextPosition tp2) {
        return Math.abs(tp1.getY() - tp2.getY());
    }

    // ==================== Options参数解析工具方法 ====================

    /**
     * 从options中获取String类型参数
     */
    public static String getStringOption(Map<String, Object> options, String key, String defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        Object value = options.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 从options中获取Double类型参数
     */
    public static Double getDoubleOption(Map<String, Object> options, String key, Double defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * 从options中获取Integer类型参数
     */
    public static Integer getIntOption(Map<String, Object> options, String key, Integer defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * 从options中获取List<String>类型参数
     */
    @SuppressWarnings("unchecked")
    public static List<String> getListOption(Map<String, Object> options, String key) {
        if (options == null || !options.containsKey(key)) {
            return null;
        }
        Object value = options.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }

    /**
     * 从字符串开头移除指定字符集中的字符
     */
    public static String trimStart(String text, String charsToTrim) {
        if (text == null || charsToTrim == null) {
            return text;
        }
        int i = 0;
        while (i < text.length() && charsToTrim.indexOf(text.charAt(i)) >= 0) {
            i++;
        }
        return text.substring(i);
    }
}
