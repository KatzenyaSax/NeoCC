package com.dafuweng.sales.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("work_log")
public class WorkLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long salesRepId;

    private Date logDate;

    private Integer callsMade;

    private Integer effectiveCalls;

    private Integer newIntentions;

    private Integer intentionClients;

    private Integer faceToFaceClients;

    private Integer signedContracts;

    private String content;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
