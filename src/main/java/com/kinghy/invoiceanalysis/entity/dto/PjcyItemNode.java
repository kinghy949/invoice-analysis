package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

@Data
public class PjcyItemNode {
    /**
     *项目名称
     */
    private String itemName;
    /**
     *单位
     */
    private String itemUnit;
    /**
     *数量
     */
    private String itemQuantity;
    /**
     *备注
     */
    private String itemRemark;
    /**
     *价格
     */
    private String itemAmount;
    /**
     *金额
     */
    private String totalAmount;
    /**
     *项目标准
     */
    private String itemStd;
    /**
     *项目编码
     */
    private String itemCode;
    /**
     *项目类型
     */
    private String itemType;
    /**
     *项目自费
     */
    private String itemAmountSelf;
    /**
     *金额百分比
     */
    private String itemAmountPercent;
    /**
     *项目详细名称
     */
    private String itemDetailName;
    /**
     *项目时间
     */
    private String issueTime;



}
