package com.dafuweng.auth.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionDao extends BaseMapper<SysPermissionEntity> {

    /**
     * 根据角色ID查询权限码列表
     * @param roleId 角色ID
     * @return 权限码列表
     */
    List<String> selectPermCodesByRoleId(@Param("roleId") Long roleId);
}
