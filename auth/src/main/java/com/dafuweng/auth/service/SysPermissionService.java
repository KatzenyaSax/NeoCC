package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;

import java.util.List;

public interface SysPermissionService {

    SysPermissionEntity getById(Long id);

    PageResponse<SysPermissionEntity> pageList(PageRequest request);

    List<SysPermissionEntity> listByStatus(Short status);

    List<SysPermissionEntity> treeList();

    List<SysPermissionEntity> listByParentId(Long parentId);

    SysPermissionEntity save(SysPermissionEntity entity);

    SysPermissionEntity update(SysPermissionEntity entity);

    void delete(Long id);
}
