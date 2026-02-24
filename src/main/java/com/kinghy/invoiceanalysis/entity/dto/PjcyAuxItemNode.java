package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

@Data
public class PjcyAuxItemNode {
    /**
     *清单项名称
     */
    private String auxItemName;
    /**
     *单位
     */
    private String auxItemUnit;
    /**
     *数量
     */
    private String auxItemQuantity;
    /**
     *备注
     */
    private String auxItemRemark;
    /**
     *金额
     */
    private String auxItemAmount;
    /**
     * 单价
     */
    private String auxItemStd;
    /**
     * 清单项代码
     */
    private String auxItemCode;
    /**
     * 对应项目代码
     */
    private String auxItemRelatedCode;
    /**
     * 对应项目名称
     */
    private String auxItemRelatedName;


}
