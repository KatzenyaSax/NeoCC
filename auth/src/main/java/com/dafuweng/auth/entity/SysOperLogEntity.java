package com.dafuweng.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("sys_oper_log")
public class SysOperLogEntity {
    
    @TableId(type = IdType.AUTO)
    private Long operId;
    
    private String title;
    
    private Integer businessType;
    
    private String method;
    
    private String requestMethod;
    
    private Integer operatorType;
    
    private String operName;
    
    private String deptName;
    
    private String operUrl;
    
    private String operIp;
    
    private String operLocation;
    
    private String operParam;
    
    private String jsonResult;
    
    private Integer status;
    
    private String errorMsg;
    
    private LocalDateTime operTime;
    
    private Long costTime;
}
