package com.kinghy.invoiceanalysis.exception;

public class StrategyNotFoundException extends BusinessException {
    public StrategyNotFoundException(String fieldName, String strategyName) {
        super(
                ErrorCode.STRATEGY_NOT_FOUND,
                "字段 " + fieldName + " 使用了不存在的策略: " + strategyName
        );
    }
}
