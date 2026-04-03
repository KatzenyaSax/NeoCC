package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("contact_record")
public class ContactRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long customerId;

    private Long salesRepId;

    private Short contactType;

    private Date contactDate;

    private String content;

    private Short intentionBefore;

    private Short intentionAfter;

    private Date followUpDate;

    @TableField("attachment_urls")
    private String attachmentUrls;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
