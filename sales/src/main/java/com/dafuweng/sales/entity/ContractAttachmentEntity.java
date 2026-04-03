package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("contract_attachment")
public class ContractAttachmentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long contractId;

    private String attachmentType;

    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private String fileMd5;

    private Long uploadBy;

    private Date uploadTime;

    @TableLogic
    private Short deleted;
}
