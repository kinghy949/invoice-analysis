package com.kinghy.invoiceanalysis.strategy;

import java.util.Map;

/**
 * 发票字段提取策略接口
 * 所有策略实现必须实现此接口
 */
public interface ExtractionStrategy {

    /**
     * 执行字段提取
     * @param context 提取上下文，包含PDF文本、位置信息、字段定义等
     * @return 提取的字段值，如果未找到返回null
     */
    String extract(ExtractionContext context);

    /**
     * 获取策略名称（与FieldDefinition中的strategy字段对应）
     * @return 策略名称，如"SAME_LINE", "BELOW", "AREA"等
     */
    String getStrategyName();

    /**
     * 验证options参数是否合法
     * @param options 字段定义中的options参数
     * @return 验证通过返回true，否则返回false
     */
    default boolean validateOptions(Map<String, Object> options) {
        return true;
    }
}
