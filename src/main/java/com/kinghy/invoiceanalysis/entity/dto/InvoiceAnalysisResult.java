package com.kinghy.invoiceanalysis.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class InvoiceAnalysisResult {
    private String fileName;
    private String templateName;
    private Map<String, String> fields;
}
