package com.dafuweng.auth.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象，比UserEntity多deptName和roleName字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserVO extends com.dafuweng.auth.entity.SysUserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 职务（角色名称，多个用逗号分隔）
     */
    private String roleName;
}