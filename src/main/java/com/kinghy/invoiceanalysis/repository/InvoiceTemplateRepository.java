package com.kinghy.invoiceanalysis.repository;

import com.kinghy.invoiceanalysis.entity.pojo.InvoiceTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 发票模板数据访问层
 */
@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplateEntity, Long> {

    /**
     * 根据模板名称查询
     */
    Optional<InvoiceTemplateEntity> findByTemplateName(String templateName);

    /**
     * 查询所有启用的模板
     */
    List<InvoiceTemplateEntity> findByEnabledTrue();

    /**
     * 根据模板名称删除
     */
    void deleteByTemplateName(String templateName);

    /**
     * 检查模板名称是否存在
     */
    boolean existsByTemplateName(String templateName);
}
