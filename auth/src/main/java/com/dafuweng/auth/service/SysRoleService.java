package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;

import java.util.List;

public interface SysRoleService {

    SysRoleEntity getById(Long id);

    PageResponse<SysRoleEntity> pageList(PageRequest request);

    List<SysRoleEntity> listByStatus(Short status);

    List<Long> getPermissionIdsByRoleId(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    SysRoleEntity save(SysRoleEntity entity);

    SysRoleEntity update(SysRoleEntity entity);

    void delete(Long id);
}
