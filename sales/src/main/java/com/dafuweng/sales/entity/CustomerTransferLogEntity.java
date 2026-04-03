package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("customer_transfer_log")
public class CustomerTransferLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long customerId;

    private Long fromRepId;

    private Long toRepId;

    private String operateType;

    private String reason;

    private Long operatedBy;

    private Date operatedAt;

    @TableLogic
    private Short deleted;
}
