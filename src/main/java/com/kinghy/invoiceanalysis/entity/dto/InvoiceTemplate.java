package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InvoiceTemplate {
    private String templateName;
    private List<String> identifiers;
    private List<FieldDefinition> fields;
}


