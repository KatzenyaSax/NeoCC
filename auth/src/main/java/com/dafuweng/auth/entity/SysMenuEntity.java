package com.dafuweng.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体类
 */
@Data
@TableName("sys_menu")
public class SysMenuEntity {
    
    @TableId(type = IdType.AUTO)
    private Long menuId;
    
    private String menuName;
    
    private Long parentId;
    
    private Integer orderNum;
    
    private String path;
    
    private String component;
    
    private String query;
    
    private String routeName;
    
    private Integer isFrame;
    
    private Integer isCache;
    
    private String menuType;
    
    private String visible;
    
    private String status;
    
    private String perms;
    
    private String icon;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
