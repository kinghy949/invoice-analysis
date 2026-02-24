package com.kinghy.invoiceanalysis.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;
import com.kinghy.invoiceanalysis.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件系统模板服务
 * 从JSON文件加载模板（用于开发调试）
 *
 * 配置: template.source=filesystem（默认）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "template.source", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemTemplateService implements TemplateService {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String LEGACY_TEMPLATE_PATH = "src/main/java/com/kinghy/invoiceanalysis/config/templates";

    @Value("${template.filesystem.path:classpath:templates/invoice}")
    private String templatesPath;

    @Value("${template.cache.enabled:false}")
    private boolean templateCacheEnabled;

    private final Map<String, InvoiceTemplate> templateCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadTemplates() {
        reloadTemplates();
    }

    @Override
    public void reloadTemplates() {
        templateCache.clear();

        try {
            if (templatesPath.startsWith(CLASSPATH_PREFIX)) {
                loadFromClasspath(templatesPath);
            } else {
                loadFromFileSystem(templatesPath);
                if (templateCache.isEmpty() && !LEGACY_TEMPLATE_PATH.equals(templatesPath)) {
                    log.warn("当前路径未加载到模板，尝试兼容旧路径: {}", LEGACY_TEMPLATE_PATH);
                    loadFromFileSystem(LEGACY_TEMPLATE_PATH);
                }
            }
            log.info("文件系统模板服务初始化完成，已加载 {} 个模板", templateCache.size());
        } catch (Exception e) {
            log.error("初始化文件系统模板服务失败", e);
        }
    }

    private void loadFromClasspath(String classpath) throws IOException {
        String basePath = classpath.substring(CLASSPATH_PREFIX.length());
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }
        String pattern = "classpath*:" + basePath + "*.json";
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                InvoiceTemplate template = objectMapper.readValue(inputStream, InvoiceTemplate.class);
                templateCache.put(template.getTemplateName(), template);
                log.info("加载模板: {} (资源: {})", template.getTemplateName(), resource.getFilename());
            } catch (Exception e) {
                log.error("加载模板资源失败: {}", resource.getFilename(), e);
            }
        }
    }

    private void loadFromFileSystem(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            log.warn("模板目录不存在: {}", path);
            return;
        }

        File[] templateFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (templateFiles == null) {
            return;
        }
        Arrays.sort(templateFiles);
        for (File file : templateFiles) {
            try {
                InvoiceTemplate template = objectMapper.readValue(file, InvoiceTemplate.class);
                templateCache.put(template.getTemplateName(), template);
                log.info("加载模板: {} (文件: {})", template.getTemplateName(), file.getName());
            } catch (Exception e) {
                log.error("加载模板文件失败: {}", file.getName(), e);
            }
        }
    }

    private boolean isClasspathPath() {
        return templatesPath != null && templatesPath.startsWith(CLASSPATH_PREFIX);
    }

    @Override
    public boolean saveTemplate(InvoiceTemplate template) {
        if (isClasspathPath()) {
            log.error("classpath模板路径为只读，无法保存模板: {}", template.getTemplateName());
            return false;
        }
        try {
            // 更新缓存
            templateCache.put(template.getTemplateName(), template);

            // 保存到文件
            String fileName = template.getTemplateName()
                    .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5-]", "-") + ".json";
            File file = new File(templatesPath, fileName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, template);

            log.info("保存模板: {} -> {}", template.getTemplateName(), file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("保存模板失败: {}", template.getTemplateName(), e);
            return false;
        }
    }

    @Override
    public boolean deleteTemplate(String templateName) {
        if (isClasspathPath()) {
            log.error("classpath模板路径为只读，无法删除模板: {}", templateName);
            return false;
        }
        InvoiceTemplate removed = templateCache.remove(templateName);
        if (removed != null) {
            // 尝试删除文件
            File dir = new File(templatesPath);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        InvoiceTemplate template = objectMapper.readValue(file, InvoiceTemplate.class);
                        if (templateName.equals(template.getTemplateName())) {
                            boolean deleted = file.delete();
                            log.info("删除模板文件: {} (成功: {})", file.getName(), deleted);
                            break;
                        }
                    } catch (Exception e) {
                        log.error("读取模板文件失败: {}", file.getName(), e);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public InvoiceTemplate findTemplateFor(String pdfTextContent) {
        ensureTemplatesReady();
        for (InvoiceTemplate template : templateCache.values()) {
            if (template.getIdentifiers() == null || template.getIdentifiers().isEmpty()) {
                continue;
            }

            boolean allIdentifiersFound = false;
            for (String identifier : template.getIdentifiers()) {
                if (pdfTextContent.contains(identifier)) {
                    allIdentifiersFound = true;
                    break;
                }
            }
            if (allIdentifiersFound) {
                log.debug("匹配到模板: {}", template.getTemplateName());
                return template;
            }
        }
        return null;
    }

    @Override
    public InvoiceTemplate getTemplateByName(String templateName) {
        ensureTemplatesReady();
        return templateCache.get(templateName);
    }

    @Override
    public List<InvoiceTemplate> getAllTemplates() {
        ensureTemplatesReady();
        return new ArrayList<>(templateCache.values());
    }

    private void ensureTemplatesReady() {
        if (!templateCacheEnabled || templateCache.isEmpty()) {
            reloadTemplates();
        }
    }
}
