package com.dafuweng.finance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("commission_record")
public class CommissionRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long performanceId;

    private Long salesRepId;

    private Long contractId;

    private BigDecimal commissionAmount;

    private BigDecimal commissionRate;

    private Short status;

    private Date confirmTime;

    private Date grantTime;

    private String grantAccount;

    private String remark;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
