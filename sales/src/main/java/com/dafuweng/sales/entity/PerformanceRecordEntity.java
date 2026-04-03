package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("performance_record")
public class PerformanceRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long contractId;

    private Long salesRepId;

    private Long deptId;

    private Long zoneId;

    private BigDecimal contractAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private Short status;

    private Date calculateTime;

    private Date confirmTime;

    private Date grantTime;

    private String cancelReason;

    private String remark;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
