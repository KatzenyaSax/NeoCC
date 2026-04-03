package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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

    @TableField("guarantee_info")
    private String guaranteeInfo;

    private String rejectReason;

    private String remark;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;

    private Integer version;
}
