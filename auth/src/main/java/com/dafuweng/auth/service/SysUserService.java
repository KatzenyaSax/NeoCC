package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.vo.UserVO;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;

import java.util.List;
import java.util.Map;

public interface SysUserService {

    SysUserEntity getById(Long id);

    PageResponse<SysUserEntity> pageList(PageRequest request);

    /**
     * 分页查询用户列表（带部门名称）
     */
    PageResponse<UserVO> pageListWithDeptName(PageRequest request);

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

    /**
     * 获取用户总数
     */
    Long count();

    /**
     * 根据部门ID查询用户ID列表
     */
    List<Long> listUserIdsByDeptId(Long deptId);

    /**
     * 根据战区ID查询用户ID列表
     */
    List<Long> listUserIdsByZoneId(Long zoneId);

    /**
     * 根据用户ID列表查询真实姓名
     */
    Map<Long, String> listRealNamesByIds(List<Long> ids);

    /**
     * 根据角色ID列表查询用户列表
     */
    List<SysUserEntity> listByRoleIds(List<Long> roleIds);

    /**
     * 获取最小可用用户ID
     */
    Long selectMinAvailableId();
}
