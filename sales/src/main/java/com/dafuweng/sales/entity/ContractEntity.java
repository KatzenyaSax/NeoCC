package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("contract")
public class ContractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String contractNo;

    private Long customerId;

    private Long salesRepId;

    private Long deptId;


    private Long productId;

    /** 战区ID */
    private Long zoneId;

    private BigDecimal contractAmount;

    private BigDecimal actualLoanAmount;

    private BigDecimal serviceFeeRate;

    @TableField("service_fee_1")
    private BigDecimal serviceFee1;

    @TableField("service_fee_2")
    private BigDecimal serviceFee2;

    @TableField("service_fee_1_paid")
    private Short serviceFee1Paid;

    @TableField("service_fee_2_paid")
    private Short serviceFee2Paid;

    @TableField("service_fee_1_pay_date")
    private Date serviceFee1PayDate;

    @TableField("service_fee_2_pay_date")
    private Date serviceFee2PayDate;

    private Short status;

    private Date signDate;

    private String paperContractNo;

    private Date financeSendTime;

    private Date financeReceiveTime;

    private String loanUse;

    @TableField("guarantee_info")
    private String guaranteeInfo;

    private String rejectReason;

    private String remark;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    private Short deleted;

    private Integer version;
}
