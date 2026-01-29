package com.kinghy.invoiceanalysis.service;

import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;

import java.util.List;

/**
 * 模板服务接口
 * 支持多种数据源实现（文件系统、数据库等）
 */
public interface TemplateService {

    /**
     * 根据PDF文本内容查找匹配的模板
     * @param pdfTextContent PDF全文文本
     * @return 匹配的模板，如果未找到返回null
     */
    InvoiceTemplate findTemplateFor(String pdfTextContent);

    /**
     * 根据模板名称获取模板
     * @param templateName 模板名称
     * @return 模板对象，如果不存在返回null
     */
    InvoiceTemplate getTemplateByName(String templateName);

    /**
     * 获取所有模板
     * @return 模板列表
     */
    List<InvoiceTemplate> getAllTemplates();

    /**
     * 保存或更新模板
     * @param template 模板对象
     * @return 保存成功返回true
     */
    boolean saveTemplate(InvoiceTemplate template);

    /**
     * 删除模板
     * @param templateName 模板名称
     * @return 删除成功返回true
     */
    boolean deleteTemplate(String templateName);

    /**
     * 重新加载模板（用于配置变更后刷新）
     */
    default void reloadTemplates() {
        // 默认空实现，子类可以覆盖
    }
}
