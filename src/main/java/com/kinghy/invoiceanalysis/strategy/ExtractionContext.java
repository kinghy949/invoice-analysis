package com.kinghy.invoiceanalysis.strategy;

import com.kinghy.invoiceanalysis.entity.dto.FieldDefinition;
import lombok.Builder;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;

import java.util.List;
import java.util.Map;

/**
 * 策略执行上下文
 * 封装策略执行所需的所有数据
 */
@Data
@Builder
public class ExtractionContext {

    /**
     * PDF文档对象
     */
    private PDDocument document;

    /**
     * 当前处理的页面编号（从1开始）
     */
    private int pageNumber;

    /**
     * 当前页面的纯文本内容
     */
    private String fullText;

    /**
     * 当前页面的所有文本位置信息
     */
    private List<TextPosition> allTextPositions;

    /**
     * 当前字段的定义（包含keywords, options等）
     */
    private FieldDefinition fieldDefinition;

    /**
     * 页面宽度
     */
    private float pageWidth;

    /**
     * 页面高度
     */
    private float pageHeight;

    /**
     * 快捷方法：获取关键字列表
     */
    public List<String> getKeywords() {
        return fieldDefinition != null ? fieldDefinition.getKeywords() : null;
    }

    /**
     * 快捷方法：获取options参数
     */
    public Map<String, Object> getOptions() {
        return fieldDefinition != null ? fieldDefinition.getOptions() : null;
    }

    /**
     * 快捷方法：获取字段名称
     */
    public String getFieldName() {
        return fieldDefinition != null ? fieldDefinition.getFieldName() : null;
    }
}
