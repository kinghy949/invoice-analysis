package com.kinghy.invoiceanalysis.exception;

public enum ErrorCode {
    INVALID_REQUEST(40001, "请求参数非法"),
    TEMPLATE_NOT_MATCHED(42201, "未匹配到可用模板"),
    STRATEGY_NOT_FOUND(50011, "提取策略不存在"),
    INVALID_TEMPLATE_OPTIONS(50012, "模板字段参数非法"),
    EXTRACTION_FAILED(50013, "字段提取失败"),
    PDF_PARSE_FAILED(50021, "PDF解析失败"),
    INTERNAL_ERROR(50000, "系统内部错误");

    private final Integer code;
    private final String defaultMessage;

    ErrorCode(Integer code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public Integer getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
