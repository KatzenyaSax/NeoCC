package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;

import java.util.List;

public interface SysUserService {

    SysUserEntity getById(Long id);

    PageResponse<SysUserEntity> pageList(PageRequest request);

    SysUserEntity getByUsername(String username);

    SysUserEntity login(String username, String password, String loginIp);

    void logout(Long userId);

    void unlock(Long userId);

    List<String> getPermCodesByUserId(Long userId);

    List<Long> getRoleIdsByUserId(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);

    SysUserEntity save(SysUserEntity entity);

    SysUserEntity update(SysUserEntity entity);

    void delete(Long id);

    List<SysUserEntity> listSalesReps();

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(Long userId, String newPassword);
}
