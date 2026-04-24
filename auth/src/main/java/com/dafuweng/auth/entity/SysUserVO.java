package com.dafuweng.auth.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户视图对象，包含关联的部门名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserVO extends SysUserEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String deptName;
}
