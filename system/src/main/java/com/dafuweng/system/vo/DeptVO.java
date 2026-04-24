package com.dafuweng.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 部门视图对象，比SysDepartmentEntity多parentDeptName、zoneName、managerName字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptVO extends com.dafuweng.system.entity.SysDepartmentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上级部门名称
     */
    private String parentDeptName;

    /**
     * 所属战区名称
     */
    private String zoneName;

    /**
     * 负责人姓名
     */
    private String managerName;
}