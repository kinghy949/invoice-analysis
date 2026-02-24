package com.kinghy.invoiceanalysis.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

//@Service
public class PositionalInvoiceExtractor {

    public static void main(String[] args) {
        // 使用您提供的其中一个PDF文件进行演示
        File pdfFile = new File("C:\\Users\\PC\\Downloads\\42060223_0001782219.pdf");

        try (PDDocument document = PDDocument.load(pdfFile)) {
            TextPositionExtractor stripper = new TextPositionExtractor();
            // 我们只处理第一页
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            stripper.getText(document); // 这会触发 writeString 并填充我们的列表

            List<TextPosition> allTextPositions = stripper.getTextPositions();

            // 1. 查找“交款人”
            String payer = findValueOnSameLine(allTextPositions, "交款人");
            System.out.println("交款人: " + (payer != null ? payer : "未找到"));

            // 2. 查找“票据号码”
            String invoiceNumber = findValueOnSameLine(allTextPositions, "票据号码");
            System.out.println("票据号码: " + (invoiceNumber != null ? invoiceNumber : "未找到"));

            // 3. 查找“金额合计(小写)”
            String totalAmount = findValueOnSameLine(allTextPositions, "金额合计(小写)");
            System.out.println("金额合计(小写): " + (totalAmount != null ? totalAmount : "未找到"));


        } catch (IOException e) {
            e.printStackTrace();
        }
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
                valuePositions.add(text);
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
