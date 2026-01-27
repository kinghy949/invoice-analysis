package com.kinghy.invoiceanalysis.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/invoice")
    public String invoiceAnalysis(){
        log.info("开始执行发票分析");
        return "success";
    }



}