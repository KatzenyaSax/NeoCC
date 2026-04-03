package com.dafuweng.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private Long deptId;

    private Long zoneId;

    private Short status;

    private Integer loginErrorCount;

    private Date lockTime;

    private Date lastLoginTime;

    private String lastLoginIp;

    private Long createdBy;

    private Date createdAt;

    private Long updatedBy;

    private Date updatedAt;

    @TableLogic
    private Short deleted;

    @Version
    private Integer version;
}
