package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.entity.SysDepartmentVO;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysDepartmentService {

    SysDepartmentEntity getById(Long id);

    /**
     * 分页查询部门列表（包含关联名称）
     */
    PageResponse<SysDepartmentVO> pageList(PageRequest request);

    List<SysDepartmentEntity> listByParentId(Long parentId);

    List<SysDepartmentEntity> listByZoneId(Long zoneId);

    /**
     * 获取所有部门列表（用于下拉选择）
     */
    List<SysDepartmentEntity> listAll();

    @Transactional
    SysDepartmentEntity save(SysDepartmentEntity entity);

    @Transactional
    SysDepartmentEntity update(SysDepartmentEntity entity);

    @Transactional
    void delete(Long id);
}
