package com.kinghy.invoiceanalysis.controller;

import com.kinghy.invoiceanalysis.entity.dto.ApiResponse;
import com.kinghy.invoiceanalysis.entity.dto.InvoiceAnalysisResult;
import com.kinghy.invoiceanalysis.exception.InvalidRequestException;
import com.kinghy.invoiceanalysis.service.InvoiceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author WangHaiYang
 * @version 1.0
 * @description: TODO
 * @date 2026/1/27 13:45
 */
@Slf4j
@RestController
@RequestMapping("/analysis")
public class InvoiceAnalysisController {
    @Autowired
    private InvoiceProcessor invoiceProcessor;

    @PostMapping("/invoice")
    public ApiResponse<InvoiceAnalysisResult> invoiceAnalysis(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidRequestException("上传文件名不能为空");
        }
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidRequestException("仅支持PDF文件");
        }

        log.info("开始执行发票分析: {}", fileName);
        try {
            InvoiceAnalysisResult result = invoiceProcessor.process(file.getInputStream(), fileName);
            return ApiResponse.success(result);
        } catch (java.io.IOException e) {
            throw new InvalidRequestException("读取上传文件失败");
        }
    }
}
