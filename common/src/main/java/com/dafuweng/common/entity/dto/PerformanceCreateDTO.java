package com.dafuweng.common.entity.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PerformanceCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 合同ID（来源：loan_audit.contract_id） */
    private Long contractId;

    /** 客户ID（来源：contract.customer_id） */
    private Long customerId;

    /** 销售代表ID（来源：contract.sales_rep_id） */
    private Long salesRepId;

    /** 部门ID（来源：contract.dept_id，金融部需要能查到这个） */
    private Long deptId;

    /** 战区ID（需要从 system 查到 sales_rep 对应的 zone_id，或由 finance 传递） */
    private Long zoneId;

    /** 合同金额（来源：contract.contract_amount） */
    private BigDecimal contractAmount;

    /** 提成比例（来源：loan_audit.approved_amount 上的金融产品 commission_rate） */
    private BigDecimal commissionRate;

    /** 提成金额（金融部计算得出：contract_amount × commission_rate） */
    private BigDecimal commissionAmount;

    /** 业绩状态（默认待计算：0 = pending） */
    private Short status;

    /** 业绩计算时间 */
    private Date calculateTime;

    /** 备注（如拒绝原因） */
    private String remark;
}