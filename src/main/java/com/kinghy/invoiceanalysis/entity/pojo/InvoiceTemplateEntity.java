package com.kinghy.invoiceanalysis.entity.pojo;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 发票模板数据库实体
 */
@Data
@Entity
@Table(name = "invoice_template")
public class InvoiceTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(name = "template_name", unique = true, nullable = false, length = 255)
    private String templateName;

    /**
     * 识别标识符JSON数组
     * 例如: ["北京市医疗门诊收费票据", "医疗门诊收费票据"]
     */
    @Column(name = "identifiers_json", columnDefinition = "TEXT", nullable = false)
    private String identifiersJson;

    /**
     * 字段定义JSON数组
     * 包含所有字段的提取规则
     */
    @Column(name = "fields_json", columnDefinition = "TEXT", nullable = false)
    private String fieldsJson;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
