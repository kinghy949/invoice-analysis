package com.kinghy.invoiceanalysis.exception;

public class InvalidTemplateOptionException extends BusinessException {
    public InvalidTemplateOptionException(String fieldName, String strategyName) {
        super(
                ErrorCode.INVALID_TEMPLATE_OPTIONS,
                "字段 " + fieldName + " 的策略 " + strategyName + " 参数校验失败"
        );
    }
}
