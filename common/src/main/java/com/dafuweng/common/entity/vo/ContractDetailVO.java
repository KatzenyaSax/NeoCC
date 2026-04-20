package com.dafuweng.common.entity.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 合同详情VO，包含合同信息及关联的客户、销售代表、部门、战区信息
 */
@Data
public class ContractDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 合同信息 */
    private ContractVO contract;

    /** 客户信息 */
    private Long customerId;
    private String customerName;
    private String customerPhone;

    /** 销售代表信息 */
    private Long salesRepId;
    private String salesRepName;

    /** 部门信息 */
    private Long deptId;
    private String deptName;

    /** 战区信息 */
    private Long zoneId;
    private String zoneName;
}
