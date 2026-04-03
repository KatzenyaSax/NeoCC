package com.dafuweng.sales.entity;

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
@TableName("customer")
public class CustomerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String name;

    private String phone;

    private String idCard;

    private String companyName;

    private String companyLegalPerson;

    private BigDecimal companyRegCapital;

    private Short customerType;

    private Long salesRepId;

    private Long deptId;

    private Long zoneId;

    private Short intentionLevel;

    private Short status;

    private Date lastContactDate;

    private Date nextFollowUpDate;

    private Date publicSeaTime;

    private String publicSeaReason;

    @TableField(value = "annotation", typeHandler = JacksonTypeHandler.class)
    private Object annotation;

    private String source;

    private BigDecimal loanIntentionAmount;

    private String loanIntentionProduct;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;

    private Integer version;
}
