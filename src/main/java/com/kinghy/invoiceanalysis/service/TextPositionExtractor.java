package com.kinghy.invoiceanalysis.service;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个自定义的PDFTextStripper，用于收集页面上所有文本及其位置信息。
 */
public class TextPositionExtractor extends PDFTextStripper {

    private final List<TextPosition> textPositions = new ArrayList<>();

    public TextPositionExtractor() throws IOException {
        super();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        this.textPositions.addAll(textPositions);
    }

    public List<TextPosition> getTextPositions() {
        return this.textPositions;
    }
}
