package com.kinghy.invoiceanalysis.exception;

public class ExtractionFailedException extends BusinessException {
    public ExtractionFailedException(String fieldName, Throwable cause) {
        super(ErrorCode.EXTRACTION_FAILED, "字段提取失败: " + fieldName, cause);
    }
}
