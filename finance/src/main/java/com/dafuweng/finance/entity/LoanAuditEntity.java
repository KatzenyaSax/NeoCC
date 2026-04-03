package com.dafuweng.finance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("loan_audit")
public class LoanAuditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long contractId;

    private Long financeSpecialistId;

    private Long recommendedProductId;

    private BigDecimal approvedAmount;

    private Integer approvedTerm;

    private BigDecimal approvedInterestRate;

    private Short auditStatus;

    private Long bankId;

    private String bankAuditStatus;

    private Date bankApplyTime;

    private Date bankFeedbackTime;

    private String bankFeedbackContent;

    private String rejectReason;

    private String auditOpinion;

    private Date auditDate;

    private Date loanGrantedDate;

    private BigDecimal actualLoanAmount;

    private BigDecimal actualInterestRate;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
