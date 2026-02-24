package com.kinghy.invoiceanalysis.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceTemplate {
    private String templateName;
    private List<String> identifiers;
    private List<FieldDefinition> fields;
    private DetailFieldsDefinition detailFields;
}


