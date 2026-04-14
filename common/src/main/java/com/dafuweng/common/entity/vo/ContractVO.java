package com.dafuweng.common.entity.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ContractVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 合同编号 */
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 销售代表ID */
    private Long salesRepId;

    /** 部门ID */
    private Long deptId;

    /** 金融产品ID */
    private Long productId;

    /** 战区ID */
    private Long zoneId;

    /** 合同金额 */
    private BigDecimal contractAmount;

    /** 实际放款金额（银行放款后填充） */
    private BigDecimal actualLoanAmount;

    /** 服务费比例 */
    private BigDecimal serviceFeeRate;

    /** 首期服务费 */
    private BigDecimal serviceFee1;

    /** 二期服务费 */
    private BigDecimal serviceFee2;

    /** 首期服务费是否已付（0=未付, 1=已付） */
    private Short serviceFee1Paid;

    /** 二期服务费是否已付 */
    private Short serviceFee2Paid;

    /** 合同状态（1=草稿, 2=已签署, 3=已发送金融部...） */
    private Short status;

    /** 签署日期 */
    private Date signDate;

    /** 纸质合同编号 */
    private String paperContractNo;

    /** 贷款用途 */
    private String loanUse;

    /** 拒单原因 */
    private String rejectReason;

    /** 备注 */
    private String remark;
}