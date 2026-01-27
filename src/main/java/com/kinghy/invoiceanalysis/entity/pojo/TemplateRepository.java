package com.kinghy.invoiceanalysis.entity.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemplateRepository {
    private final List<InvoiceTemplate> templates = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TemplateRepository(String templatesDirectoryPath) throws IOException {
        File dir = new File(templatesDirectoryPath);
        File[] templateFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (templateFiles != null) {
            for (File file : templateFiles) {
                templates.add(objectMapper.readValue(file, InvoiceTemplate.class));
            }
        }
    }

    public InvoiceTemplate findTemplateFor(String pdfTextContent) {
        for (InvoiceTemplate template : templates) {
            boolean allIdentifiersFound = true;
            for (String identifier : template.getIdentifiers()) {
                if (!pdfTextContent.contains(identifier)) {
                    allIdentifiersFound = false;
                    break;
                }
            }
            if (allIdentifiersFound) {
                return template; // 找到了匹配的模板
            }
        }
        return null; // 没有找到匹配的模板
    }
}
