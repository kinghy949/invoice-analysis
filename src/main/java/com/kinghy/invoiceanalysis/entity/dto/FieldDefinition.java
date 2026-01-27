package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FieldDefinition {
    private String fieldName;
    private List<String> keywords;
    private String strategy;
    private Map<String, Object> options;
}