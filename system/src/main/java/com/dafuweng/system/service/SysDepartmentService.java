package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.vo.DeptVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public interface SysDepartmentService {

    SysDepartmentEntity getById(Long id);

    PageResponse<SysDepartmentEntity> pageList(PageRequest request);

    /**
     * 分页查询部门列表（带上级部门名称、战区名称、负责人姓名）
     */
    PageResponse<DeptVO> pageListWithDetails(PageRequest request);

    List<SysDepartmentEntity> listByParentId(Long parentId);

    List<SysDepartmentEntity> listByZoneId(Long zoneId);

    /**
     * 根据部门ID列表查询部门名称（返回ID->名称映射）
     */
    Map<Long, String> listNamesByIds(List<Long> ids);

    @Transactional
    SysDepartmentEntity save(SysDepartmentEntity entity);

    @Transactional
    SysDepartmentEntity update(SysDepartmentEntity entity);

    @Transactional
    void delete(Long id);
}
