package com.kinghy.invoiceanalysis.exception;

public class PdfParseException extends BusinessException {
    public PdfParseException(String message, Throwable cause) {
        super(ErrorCode.PDF_PARSE_FAILED, message, cause);
    }
}
