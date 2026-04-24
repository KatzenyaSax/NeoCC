package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserRoleDao extends BaseMapper<SysUserRoleEntity> {

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(@Param("userId") Long userId);

    void insertBatch(@Param("list") List<SysUserRoleEntity> userRoles);

    @Select("<script>SELECT DISTINCT user_id FROM sys_user_role WHERE role_id IN <foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>#{roleId}</foreach></script>")
    List<Long> selectUserIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Select("<script>SELECT user_id, role_id FROM sys_user_role WHERE user_id IN <foreach collection='userIds' item='userId' open='(' separator=',' close=')'>#{userId}</foreach></script>")
    List<Map<String, Object>> selectUserRoleMappings(@Param("userIds") List<Long> userIds);
}
