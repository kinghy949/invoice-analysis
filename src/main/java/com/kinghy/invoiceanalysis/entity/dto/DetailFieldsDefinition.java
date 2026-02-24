package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetailFieldsDefinition {
    private List<String> tableIdentifiers;
    private List<DetailFieldColumn> columns;
}
