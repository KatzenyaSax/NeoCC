package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysPermissionDao extends BaseMapper<SysPermissionEntity> {

    /**
     * 根据角色ID查询权限码列表
     * @param roleId 角色ID
     * @return 权限码列表
     */
    List<String> selectPermCodesByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT MIN(t1.id + 1) FROM sys_permission t1 WHERE NOT EXISTS (SELECT 1 FROM sys_permission t2 WHERE t2.id = t1.id + 1 AND t2.deleted = 0)")
    Long selectMinUnusedId();

    @Update("UPDATE sys_permission SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}