package com.dafuweng.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门VO，包含关联查询的名称字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDepartmentVO extends SysDepartmentEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 上级部门名称
     */
    private String parentName;

    /**
     * 所属区域名称
     */
    private String zoneName;

    /**
     * 负责人姓名
     */
    private String managerName;
}
