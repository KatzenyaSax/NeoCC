package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;

import java.util.List;

public interface SysRoleService {

    SysRoleEntity getById(Long id);

    PageResponse<SysRoleEntity> pageList(PageRequest request);

    List<SysRoleEntity> listByStatus(Short status);

    /**
     * 根据用户ID查询角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表，如 ["SUPER_ADMIN", "ZONE_DIRECTOR"]
     */
    List<String> getRoleCodesByUserId(Long userId);

    List<Long> getPermissionIdsByRoleId(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    SysRoleEntity save(SysRoleEntity entity);

    SysRoleEntity update(SysRoleEntity entity);

    void delete(Long id);

    /**
     * 获取最小未使用的角色ID
     */
    Long getMinUnusedId();
}
