package com.kinghy.invoiceanalysis.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinghy.invoiceanalysis.entity.dto.FieldDefinition;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;
import com.kinghy.invoiceanalysis.entity.pojo.InvoiceTemplateEntity;
import com.kinghy.invoiceanalysis.repository.InvoiceTemplateRepository;
import com.kinghy.invoiceanalysis.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据库模板服务
 * 从MySQL数据库加载模板（用于生产环境）
 *
 * 配置: template.source=database
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "template.source", havingValue = "database")
public class DatabaseTemplateService implements TemplateService {

    @Autowired
    private InvoiceTemplateRepository templateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public InvoiceTemplate findTemplateFor(String pdfTextContent) {
        List<InvoiceTemplateEntity> allEntities = templateRepository.findByEnabledTrue();

        for (InvoiceTemplateEntity entity : allEntities) {
            try {
                InvoiceTemplate template = entityToDto(entity);
                
                if (template.getIdentifiers() == null || template.getIdentifiers().isEmpty()) {
                    continue;
                }

                boolean allIdentifiersFound = true;
                for (String identifier : template.getIdentifiers()) {
                    if (!pdfTextContent.contains(identifier)) {
                        allIdentifiersFound = false;
                        break;
                    }
                }

                if (allIdentifiersFound) {
                    log.debug("匹配到数据库模板: {}", template.getTemplateName());
                    return template;
                }
            } catch (Exception e) {
                log.error("解析数据库模板失败: {}", entity.getTemplateName(), e);
            }
        }

        return null;
    }

    @Override
    public InvoiceTemplate getTemplateByName(String templateName) {
        return templateRepository.findByTemplateName(templateName)
                .map(this::entityToDto)
                .orElse(null);
    }

    @Override
    public List<InvoiceTemplate> getAllTemplates() {
        List<InvoiceTemplate> templates = new ArrayList<>();
        for (InvoiceTemplateEntity entity : templateRepository.findAll()) {
            try {
                templates.add(entityToDto(entity));
            } catch (Exception e) {
                log.error("转换模板失败: {}", entity.getTemplateName(), e);
            }
        }
        return templates;
    }

    @Override
    @Transactional
    public boolean saveTemplate(InvoiceTemplate template) {
        try {
            InvoiceTemplateEntity entity;
            
            // 检查是否已存在
            Optional<InvoiceTemplateEntity> existing = templateRepository
                    .findByTemplateName(template.getTemplateName());
            
            if (existing.isPresent()) {
                entity = existing.get();
            } else {
                entity = new InvoiceTemplateEntity();
                entity.setTemplateName(template.getTemplateName());
            }
            
            // 更新字段
            entity.setIdentifiersJson(objectMapper.writeValueAsString(template.getIdentifiers()));
            entity.setFieldsJson(objectMapper.writeValueAsString(template.getFields()));
            entity.setEnabled(true);
            
            templateRepository.save(entity);
            log.info("保存数据库模板: {}", template.getTemplateName());
            return true;
        } catch (Exception e) {
            log.error("保存数据库模板失败: {}", template.getTemplateName(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteTemplate(String templateName) {
        try {
            if (templateRepository.existsByTemplateName(templateName)) {
                templateRepository.deleteByTemplateName(templateName);
                log.info("删除数据库模板: {}", templateName);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除数据库模板失败: {}", templateName, e);
            return false;
        }
    }

    /**
     * Entity转DTO
     */
    private InvoiceTemplate entityToDto(InvoiceTemplateEntity entity) {
        try {
            InvoiceTemplate template = new InvoiceTemplate();
            template.setTemplateName(entity.getTemplateName());

            // 解析identifiers JSON字符串
            List<String> identifiers = objectMapper.readValue(
                    entity.getIdentifiersJson(),
                    new TypeReference<List<String>>() {}
            );
            template.setIdentifiers(identifiers);

            // 解析fields JSON字符串
            List<FieldDefinition> fields = objectMapper.readValue(
                    entity.getFieldsJson(),
                    new TypeReference<List<FieldDefinition>>() {}
            );
            template.setFields(fields);

            return template;
        } catch (Exception e) {
            throw new RuntimeException("模板转换失败: " + entity.getTemplateName(), e);
        }
    }
}
