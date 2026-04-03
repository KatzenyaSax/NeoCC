package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysDepartmentEntity;
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

public interface SysDepartmentService {

    SysDepartmentEntity getById(Long id);

    PageResponse<SysDepartmentEntity> pageList(PageRequest request);

    List<SysDepartmentEntity> listByParentId(Long parentId);

    List<SysDepartmentEntity> listByZoneId(Long zoneId);

    @Transactional
    SysDepartmentEntity save(SysDepartmentEntity entity);

    @Transactional
    SysDepartmentEntity update(SysDepartmentEntity entity);

    @Transactional
    void delete(Long id);
}
