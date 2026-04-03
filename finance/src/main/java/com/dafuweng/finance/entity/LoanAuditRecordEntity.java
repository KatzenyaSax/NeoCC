package com.dafuweng.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("loan_audit_record")
public class LoanAuditRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long loanAuditId;

    private Long operatorId;

    private String operatorName;

    private String operatorRole;

    private String action;

    private String content;

    @TableField("attachment_urls")
    private String attachmentUrls;

    private Date createdAt;
}
