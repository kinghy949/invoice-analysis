package com.kinghy.invoiceanalysis.exception;

public class TemplateNotMatchedException extends BusinessException {
    public TemplateNotMatchedException(String fileName) {
        super(ErrorCode.TEMPLATE_NOT_MATCHED, "未找到匹配模板，文件: " + fileName);
    }
}
