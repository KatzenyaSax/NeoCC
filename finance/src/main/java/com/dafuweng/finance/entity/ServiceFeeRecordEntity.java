package com.dafuweng.finance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("service_fee_record")
public class ServiceFeeRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long contractId;

    private Short feeType;

    private BigDecimal amount;

    private BigDecimal shouldAmount;

    private String paymentMethod;

    private Short paymentStatus;

    private Date paymentDate;

    private String paymentAccount;

    private String receiptNo;

    private Long accountantId;

    private String remark;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
