package com.dafuweng.common.mq.event;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LoanApprovedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long loanAuditId;
    private Long contractId;
    private Long customerId;
    private Long salesRepId;
    private Long deptId;
    private Long zoneId;
    private BigDecimal actualLoanAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private Date loanGrantedDate;
}
