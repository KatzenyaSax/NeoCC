package com.dafuweng.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("finance_product")
public class FinanceProductEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String productCode;

    private String productName;

    private Long bankId;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal interestRate;

    private Integer minTerm;

    private Integer maxTerm;

    @TableField(value = "requirements", typeHandler = JacksonTypeHandler.class)
    private Object requirements;

    @TableField(value = "documents", typeHandler = JacksonTypeHandler.class)
    private Object documents;

    private String productFeatures;

    private BigDecimal commissionRate;

    private Short status;

    private Integer sortOrder;

    private Date onlineTime;

    private Date offlineTime;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
