package com.kinghy.invoiceanalysis.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 非税财政票据5要素查询响应数据对象
 */
@Data
public class PjcyDataNode {
    /**
     * 转义后的响应状态代码
     */
    private String code;
    /**
     * 转义后的响应状态提供给外部的代码
     */
    private String viewCode;
    /**
     * 转义后的响应状态描述
     */
    private String message;


    /**
     * 收款单位（开票单位）
     */
    private String invoicingPartyName;
    /**
     * 复核人
     */
    private String checker;
    /**
     * 开票人
     */
    private String handlingPerson;
    /**
     * 就诊日期
     */
    private String medicalDate;
    /**
     * 医保编号
     */
    private String medicalInsuranceID;
    /**
     * 医保类型
     */
    private String medicalInsuranceType;
    /**
     * 医疗机构类型
     */
    private String orgType;
    /**
     * 门诊号
     */
    private String patientNumber;
    /**
     * 其他支付
     */
    private String otherPayAmount;
    /**
     * 个人自费
     */
    private String selfpaymentCost;
    /**
     * 业务流水号
     */
    private String bizCode;
    /**
     * 个人账户支付
     */
    private String accountPayAmount;
    /**
     * 个人自付
     */
    private String selfpaymentAmount;
    /**
     * 门诊大额支付
     */
    private String outpatientLargePayments;
    /**
     * 性别
     */
    private String gender;
    /**
     * 个人现金支付
     */
    private String ownPayAmount;
    /**
     * 医保统筹基金支付
     */
    private String fundPayAmount;
    /**
     * 交款人
     */
    private String payerPartyName;
    /**
     * 交款人统一社会信用代码
     */
    private String payerPartyCode;
    /**
     * 票据代码
     */
    private String eInvoiceCode;
    /**
     * 票据号码
     */
    private String eInvoiceNumber;
    /**
     * 校验码
     */
    private String randomNumber;
    /**
     * 电子票据名称
     */
    private String eInvoiceName;
    /**
     * 开票日期
     */
    private String issueDate;
    /**
     * 开票单位印章名称
     */
    private String invoicingSealName;
    /**
     * 印章Hash
     */
    private String invoicingSealHash;
    /**
     * 开票单位印章编号
     */
    private String invoicingSealId;
    /**
     * 财务部门印章
     */
    private String supervisorSealName;
    /**
     * 印章Hash
     */
    private String supervisorSealHash;
    /**
     * 财政部门印章编号
     */
    private String supervisorSealId;
    /**
     * 个人承担
     */
    private String selfPaymentCare;
    /**
     * 本年支付
     */
    private String individualAccountCurrPayment;
    /**
     * 历年支付
     */
    private String individualAccountPrevPayment;
    /**
     * 本年余额
     */
    private String individualAccountCurrBalance;
    /**
     * 历年余额
     */
    private String individualAccountPrevBalance;
    /**
     * 病历号
     */
    private String casenumber;
    /**
     * 财政部门编码
     */
    private String supervisorAreaCode;
    /**
     * 出院日期
     */
    private String outHospitalDate;
    /**
     * 相关票据代码（预留扩展字段，开具红票时在此写）
     */
    private String relatedInvoiceCode;
    /**
     * 相关票据号码（预留扩展字段，开具红票时在此写）
     */
    private String relatedInvoiceNumber;
    /**
     * （住院）科别
     */
    private String departmentName;
    /**
     * 退费金额
     */
    private String refundAmount;
    /**
     * 业务单号
     */
    private String businessNumber;
    /**
     * 业务日期
     */
    private String businessDate;
    /**
     * 缴款码
     */
    private String payCode;
    /**
     * 住院日期
     */
    private String inHospitalDate;
    /**
     * 补缴金额
     */
    private String rechargeAmount;
    /**
     * 交款人账号
     */
    private String payerAcct;
    /**
     * 交款人开户行
     */
    private String payerOpBk;
    /**
     * 交款人类型
     */
    private String payerPartyType;
    /**
     * 汇率
     */
    private String exchangeRate;
    /**
     * 电子票据模板代码
     */
    private String eInvoiceSpecimenCode;
    /**
     * 交款方式
     */
    private String payMode;
    /**
     * 开票单位代码
     */
    private String invoicingPartyCode;
    /**
     * 收款人开户行
     */
    private String recOpBk;
    /**
     * 收款人全称
     */
    private String recName;
    /**
     * 收款人账号
     */
    private String recAcct;
    /**
     * 备注
     */
    private String remark;
    /**
     * 货币种类
     */
    private String currencyType;
    /**
     * 财务部门备注
     */
    private String supervisorRemark;
    /**
     * 年龄
     */
    private String age;
    /**
     * 医学类型
     */
    private String medicalType;
    /**
     * 医保统筹基金支付明细
     */
    private String fundPayAmountInfo;
    /**
     * 其他支付明细
     */
    private String otherPayAmountInfo;
    /**
     * 患者单位
     */
    private String patientCompany;
    /**
     * 手机号码
     */
    private String phoneNumber;
    /**
     * 预付款
     */
    private String prepayAmount;
    /**
     * 区域注册码
     */
    private String regiCode;
    /**
     * 打印日期
     */
    private String printDate;
    /**
     * 打印时间
     */
    private String printTime;
    /**
     * TODO
     */
    private String printed;
    /**
     * 进入标识
     */
    private String enterGuid;
    /**
     * TODO
     */
    private String voided;
    /**
     * 取消日期
     */
    private String voidDate;
    /**
     * 取消时间
     */
    private String voidTime;
    /**
     * 取消原因
     */
    private String voidReason;
    /**
     * 订阅ID
     */
    private String bookId;
    /**
     * TODO
     */
    private String accessId;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 最后更新时间
     */
    private String updateTime;
    /**
     * 电子状态
     */
    private String eInvoiceStatus;
    /**
     * 金额百分比
     */
    private String itemAmountPercent;
    /**
     * 打印代码
     */
    private String printCode;
    /**
     * 打印编码
     */
    private String printNumber;
    /**
     * 级别
     */
    private String classes;
    /**
     * 住院号
     */
    private String hospitalizationNumber;
    /**
     * 学号
     */
    private String studentID;
    /**
     * 学校
     */
    private String school;
    /**
     * 院
     */
    private String faculty;
    /**
     * 地址
     */
    private String address;
    /**
     * 组织
     */
    private String cusOrganization;
    /**
     * 现金退款
     */
    private String cusRefundCash;
    /**
     * 本年余额
     */
    private String cusCurrentYearAccount;
    /**
     * 历年余额
     */
    private String cusPastYearAccount;
    /**
     * 自付现金总计
     */
    private String cusSelfpaymentCashAddup;
    /**
     * 医疗职业保险
     */
    private String cusMedicalInsuranceFraternity;
    /**
     * 充值现金
     */
    private String cusRechargeCash;
    /**
     * 大额保险
     */
    private String cusLargeInsurance;
    /**
     * 保险账户支付总计
     */
    private String cusSelfpaymentAmountAddup;
    /**
     * 充值检查
     */
    private String cusRechargeCheck;
    /**
     * 项目列表
     */
    private List<PjcyItemNode> itemList;
    /**
     * 清单列表
     */
    private List<PjcyAuxItemNode> auxItemList;
    /**
     * 开票时间
     */
    private String issueTime;
    /**
     * 总金额
     */
    private String totalAmount;
    /**
     * 是否已冲红，true-已冲红，false-未冲红
     */
    private Boolean reversal;

    /**
     * 住院天数
     */
    private String hospitalDay;

    /**********************20210812新增其他信息内容***********************/

    /**
     * 分类自负
     */
    private String classifypaymentAmount;
    /**
     * 个人自负
     */
    private String selfconceitedAmount;
    /**
     * 附加基金支付
     */
    private String additionalFundPay;
    /**
     * 医保当年账户余额
     */
    private String medicalThatBalance;
    /**
     * 医保历年账户余额
     */
    private String medicalOverBalance;
    /**
     * 政策性减免
     */
    private String policyRelief;
    /**
     * 医院承担
     */
    private String hospitalBear;
    /**
     * 道路救助基金垫付
     */
    private String fundDjjzAmount;
    /**
     * 个人账户支付明细，作废
     */
    @Deprecated
    private String accountPayAmountInfo;
    /**
     * 个人现金支付明细
     */
    private String ownPayAmountInfo;
    /**
     * 道交救助基金垫付信息
     */
    private String fundDjjzAmountInfo;


    /**********************20210819新增模板类型***********************/
    /**
     * 模板类型，02-门诊
     */
    private String templateType;

    /**********************20210824新增其他信息内容***********************/
    /**
     * 大病保险支付
     */
    private String custom1;
    /**
     * 其他保险支付
     */
    private String custom2;
    /**
     * 医疗救助支付
     */
    private String custom3;
    /**
     * 个人账户余额
     */
    private String custom4;
    /**
     * 支付方式
     */
    private String custom5;

    /**********************20210901新增来源***********************/
    /**
     * 来源方式 ：1 接口 2.财政文件
     */
    private String fromCode;

    /**
     * 是否已冲红，true-已换开，false-未换开
     */
    private Boolean exchangePaper;

    /**********************20210927财政预览地址***********************/
    /**
     * 财政预览地址
     */
    private String czPreviewUrl;

    /**********************20211109新增其他信息内容（河南）***********************/
    /**
     * 起付标准
     */
    private String spStand;
    /**
     * 乙类首自付
     */
    private String classbSelfpayment;
    /**
     * 按比例自付
     */
    private String ptnSelfpayment;
    /**
     * 公务员补助
     */
    private String civilSubsidy;
    /**
     * 师职补助
     */
    private String teacherSubsidy;
    /**
     * 大额（病）保险报销
     */
    private String largeInsuranceReimburse;
    /**
     * 大病补充保险报销
     */
    private String largeSuppleInsuranceReimburse;
    /**
     * 医疗救助
     */
    private String medicalHelp;
    /**
     * 产前检查
     */
    private String antenatalClinic;
    /**
     * 社会保障卡号
     */
    private String medicalInsuranceNumber;
    /**
     * 自付一
     */
    private String payOne;
    /**
     * 自付二
     */
    private String payTwo;

    /**
     * 超限价自费费用——河南
     */
    private String overLimitPriceOwnExpense;

    /**
     * 文件base64
     */
    private transient String fileBase64;

    /**
     * 乙类先行自付
     */
    private String classBAdvanceSelfPayment;

    /**
     * 大病基金支付
     */
    private String criticalIllnessFund;


    /**
     * 明细查验状态 0-待查验 1-查验中 2-查验异常 3-查验成功  4-无明细
     */
    private Integer checkDetailState;

    /**
     * 存放异步下载所需token
     */
    private String tokenDown;

}
