package com.dafuweng.sales.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同详情VO，含关联名称
 */
@Data
public class ContractDetailVO implements Serializable {

    private Long id;

    private String contractNo;

    // 客户信息
    private Long customerId;
    private String customerName;

    // 销售代表信息
    private Long salesRepId;
    private String salesRepName;

    // 部门信息
    private Long deptId;
    private String deptName;

    // 战区信息
    private Long zoneId;
    private String zoneName;

    // 产品信息
    private Long productId;
    private String productName;

    private BigDecimal contractAmount;

    private BigDecimal actualLoanAmount;

    private BigDecimal serviceFeeRate;

    private BigDecimal serviceFee1;

    private BigDecimal serviceFee2;

    private Short serviceFee1Paid;

    private Short serviceFee2Paid;

    private Date serviceFee1PayDate;

    private Date serviceFee2PayDate;

    private Short status;

    private Date signDate;

    private String paperContractNo;

    private Date financeSendTime;

    private Date financeReceiveTime;

    private String loanUse;

    private String guaranteeInfo;

    private String rejectReason;

    private String remark;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;
}