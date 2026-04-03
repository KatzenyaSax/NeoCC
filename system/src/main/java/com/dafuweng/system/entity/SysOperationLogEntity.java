package com.dafuweng.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_operation_log")
public class SysOperationLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String action;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String responseCode;

    private String errorMsg;

    private String ip;

    private String userAgent;

    private Long costTimeMs;

    private Date createdAt;
}
