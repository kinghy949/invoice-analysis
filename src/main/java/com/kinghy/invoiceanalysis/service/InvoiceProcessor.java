package com.kinghy.invoiceanalysis.service;

import com.kinghy.invoiceanalysis.entity.dto.FieldDefinition;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceAnalysisResult;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;
import com.kinghy.invoiceanalysis.exception.ExtractionFailedException;
import com.kinghy.invoiceanalysis.exception.InvalidTemplateOptionException;
import com.kinghy.invoiceanalysis.exception.PdfParseException;
import com.kinghy.invoiceanalysis.exception.StrategyNotFoundException;
import com.kinghy.invoiceanalysis.exception.TemplateNotMatchedException;
import com.kinghy.invoiceanalysis.strategy.ExtractionContext;
import com.kinghy.invoiceanalysis.strategy.ExtractionStrategy;
import com.kinghy.invoiceanalysis.strategy.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发票处理器（策略模式重构版）
 * 消除if-else分支，通过策略工厂动态选择策略
 */
@Slf4j
@Service
public class InvoiceProcessor {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private StrategyFactory strategyFactory;

    /**
     * 处理PDF发票文件
     * @param pdfFile PDF文件
     * @return 提取的字段Map
     */
    public InvoiceAnalysisResult process(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return processDocument(document, pdfFile.getName());
        } catch (IOException e) {
            throw new PdfParseException("PDF文件解析失败: " + pdfFile.getName(), e);
        }
    }

    /**
     * 处理PDF发票输入流
     * @param inputStream PDF输入流
     * @param fileName 文件名（用于日志）
     * @return 提取的字段Map
     */
    public InvoiceAnalysisResult process(InputStream inputStream, String fileName) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            return processDocument(document, fileName);
        } catch (IOException e) {
            throw new PdfParseException("PDF输入流解析失败: " + fileName, e);
        }
    }

    /**
     * 处理PDF文档
     */
    private InvoiceAnalysisResult processDocument(PDDocument document, String fileName) throws IOException {
        Map<String, String> extractedData = new HashMap<>();

        // 1. 提取纯文本
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);
        String fullText = textStripper.getText(document);
        log.debug("提取PDF文本，长度: {}", fullText.length());

        // 2. 提取带位置的文本
        TextPositionExtractor positionExtractor = new TextPositionExtractor();
        positionExtractor.setStartPage(1);
        positionExtractor.setEndPage(1);
        positionExtractor.getText(document);
        List<TextPosition> allTextPositions = positionExtractor.getTextPositions();
        log.debug("提取文本位置信息，总字符数: {}", allTextPositions.size());

        // 3. 查找匹配的模板
        InvoiceTemplate template = templateService.findTemplateFor(fullText);
        if (template == null) {
            log.warn("未找到匹配的模板: {}", fileName);
            throw new TemplateNotMatchedException(fileName);
        }
        log.info("使用模板: {}", template.getTemplateName());

        // 4. 获取页面尺寸
        PDPage page = document.getPage(0);
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // 5. 遍历字段，使用策略模式提取（消除if-else）
        for (FieldDefinition field : template.getFields()) {
            String strategyName = field.getStrategy();

            // 从工厂获取策略
            ExtractionStrategy strategy = strategyFactory.getStrategy(strategyName);
            if (strategy == null) {
                log.error("字段 {} 使用了未知策略: {}", field.getFieldName(), strategyName);
                throw new StrategyNotFoundException(field.getFieldName(), strategyName);
            }

            // 验证options参数
            if (!strategy.validateOptions(field.getOptions())) {
                log.error("字段 {} 的options参数验证失败", field.getFieldName());
                throw new InvalidTemplateOptionException(field.getFieldName(), strategyName);
            }

            // 构建上下文
            ExtractionContext context = ExtractionContext.builder()
                    .document(document)
                    .pageNumber(1)
                    .fullText(fullText)
                    .allTextPositions(allTextPositions)
                    .fieldDefinition(field)
                    .pageWidth(pageWidth)
                    .pageHeight(pageHeight)
                    .build();

            // 执行策略
            try {
                String value = strategy.extract(context);
                if (value != null && !value.isEmpty()) {
                    extractedData.put(field.getFieldName(), value);
                    log.info("提取字段成功: {} = {}", field.getFieldName(), value);
                } else {
                    log.warn("字段 {} 提取结果为空", field.getFieldName());
                }
            } catch (Exception e) {
                log.error("字段 {} 提取失败: {}", field.getFieldName(), e.getMessage(), e);
                throw new ExtractionFailedException(field.getFieldName(), e);
            }
        }

        return new InvoiceAnalysisResult(fileName, template.getTemplateName(), extractedData);
    }
}
