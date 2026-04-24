package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String sortField;
    private String sortOrder = "asc";
    private String name;
    private Short status;           // 状态：0-无效，1-有效，5-公海
    private Short customerType;     // 客户类型：1-个人，2-企业
    private Short intentionLevel;   // 意向等级：1-低，2-中，3-高，4-很有意向

    // 用户管理搜索字段
    private String username;
    private String realName;

    // 角色管理搜索字段
    private String roleName;
    private String roleCode;

    // 部门管理搜索字段
    private String deptName;

    // 权限管理搜索字段
    private String permName;
    private String permCode;
    private Short permType;

    // 战区管理搜索字段
    private String zoneName;
}
