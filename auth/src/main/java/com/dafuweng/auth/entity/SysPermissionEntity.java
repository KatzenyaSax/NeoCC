package com.dafuweng.auth.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_permission")
public class SysPermissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long parentId;

    private String permCode;

    private String permName;

    private Short permType;

    private String permPath;

    private String icon;

    private Integer sortOrder;

    private Short status;

    private Short externalLink;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
