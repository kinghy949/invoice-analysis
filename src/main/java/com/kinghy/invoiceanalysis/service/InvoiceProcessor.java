package com.kinghy.invoiceanalysis.service;

import com.kinghy.invoiceanalysis.entity.dto.FieldDefinition;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceTemplate;
import com.kinghy.invoiceanalysis.entity.pojo.TemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j

public class InvoiceProcessor {

    private final TemplateRepository templateRepository;
    // 实际项目中，这里应该用策略模式注入不同的Strategy实现
    // private final Map<String, ExtractionStrategy> strategies; 

    public InvoiceProcessor(String templatesPath) throws IOException {
        this.templateRepository = new TemplateRepository(templatesPath);
    }

    public Map<String, String> process(File pdfFile) throws IOException {
        Map<String, String> extractedData = new HashMap<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            // 1. 提取带位置的文本和纯文本
            PDFTextStripper textStripper = new PDFTextStripper();

            TextPositionExtractor stripper = new TextPositionExtractor();
            // 我们只处理第一页
            textStripper.setStartPage(1);
            textStripper.setEndPage(1);
            textStripper.getText(document); // 这会触发 writeString 并填充我们的列表

            String fullText = textStripper.getText(document);
            log.info("PDF文本: " + fullText);

            List<TextPosition> allTextPositions = stripper.getTextPositions();

            // 2. 查找匹配的模板
            InvoiceTemplate template = templateRepository.findTemplateFor(fullText);
            if (template == null) {
                System.out.println("没有找到适用于 " + pdfFile.getName() + " 的模板。");
                // 在生产中，这里可以把任务标记为“需要人工处理”或“进入二级AI处理”
                return extractedData;
            }
             System.out.println("使用模板: " + template.getTemplateName());

            // 3. 遍历模板中的字段并执行提取
            for (FieldDefinition field : template.getFields()) {
                // 这是一个简化的实现，实际应使用策略模式
                String value = null;
                if ("SAME_LINE".equals(field.getStrategy())) {
                    // 调用我们之前写的findValueOnSameLine，并传入options
                    // value = SameLineStrategy.extract(...)
                    String valueOnSameLine = findValueOnSameLine(allTextPositions, field.getKeywords().get(0));
                    value = valueOnSameLine.replaceAll("^[\\s:]+", "").trim();
                    System.out.println("字段: " + field.getFieldName() + ", 值: " + value);

                } else if ("BELOW".equals(field.getStrategy())) {
                    // 调用BELOW策略的实现
                } else if ("AREA".equals(field.getStrategy())) {
                    // 调用AREA策略的实现
                }
                
                if (value != null) {
                    extractedData.put(field.getFieldName(), value);
                }
            }
        }
        return extractedData;
    }
    
    public static void main(String[] args) throws IOException {
        InvoiceProcessor processor = new InvoiceProcessor("src/main/java/com/kinghy/invoiceanalysis/config/templates");
        File file = new File("C:\\Users\\PC\\Downloads\\11060125_0064077272.pdf");
        Map<String, String> data = processor.process(file);
        
        data.forEach((key, value) -> System.out.println(key + ": " + value));
    }




    /**
     * 在关键字的同一行右侧查找值
     * @param allTextPositions 页面上所有文本的位置信息
     * @param keyword 关键字
     * @return 找到的值，否则返回null
     */
    private static String findValueOnSameLine(List<TextPosition> allTextPositions, String keyword) {
        TextPosition keywordPosition = findKeywordPosition(allTextPositions, keyword);

        if (keywordPosition == null) {
            return null;
        }

        // 定义查找区域 (ROI - Region of Interest)
        // Y坐标：关键字的Y坐标上下一点容差（例如，半个字符高度）
        // X坐标：从关键字的右侧开始，到页面末尾（或一个合理的最大宽度）
        float yTolerance = keywordPosition.getHeight() / 2;
        float roiY_start = keywordPosition.getY() - yTolerance;
        float roiY_end = keywordPosition.getY() + keywordPosition.getHeight() + yTolerance;
        float roiX_start = keywordPosition.getX() + keywordPosition.getWidth();

        List<TextPosition> valuePositions = new ArrayList<>();
        for (TextPosition text : allTextPositions) {
            // 检查文本是否在定义的ROI内
            if (text.getY() >= roiY_start && text.getY() <= roiY_end && text.getX() >= roiX_start) {
                // 确保不是关键字本身的一部分（在非常紧凑的布局中可能发生）
                if(text.getX() > keywordPosition.getEndX()){
                    valuePositions.add(text);
                }
            }
        }

        if (valuePositions.isEmpty()) {
            return null;
        }

        // 将找到的字符按X坐标排序，然后拼接成字符串
        valuePositions.sort(Comparator.comparing(TextPosition::getX));
        StringBuilder result = new StringBuilder();
        for(TextPosition tp : valuePositions){
            result.append(tp.getUnicode());
        }

        // 清理一下可能存在的冒号和空格
        return result.toString().replaceAll("^[\\s:]+", "").trim();
    }

    /**
     * 在文本列表中查找并定位一个关键字
     * @param allTextPositions 页面上所有文本的位置信息
     * @param keyword 要查找的关键字
     * @return 关键字最后一个字符的TextPosition，如果未找到则返回null
     */
    private static TextPosition findKeywordPosition(List<TextPosition> allTextPositions, String keyword) {
        for (int i = 0; i <= allTextPositions.size() - keyword.length(); i++) {
            // 构造一个与关键字等长的字符串进行比较
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < keyword.length(); j++) {
                sb.append(allTextPositions.get(i + j).getUnicode());
            }

            if (keyword.equals(sb.toString())) {
                // 找到了！返回关键字最后一个字符的位置信息，因为我们需要它的结束坐标。
                return allTextPositions.get(i + keyword.length() - 1);
            }
        }
        return null;
    }
}
