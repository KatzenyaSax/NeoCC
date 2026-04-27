package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {

    SysUserEntity selectByUsername(@Param("username") String username);

    List<SysUserEntity> selectByRoleCode(@Param("roleCode") String roleCode);

    @Select("SELECT id FROM sys_user WHERE deleted = 0 AND dept_id = #{deptId}")
    List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);

    @Select("SELECT id FROM sys_user WHERE deleted = 0 AND zone_id = #{zoneId}")
    List<Long> selectUserIdsByZoneId(@Param("zoneId") Long zoneId);

    @Select("<script>SELECT id, real_name FROM sys_user WHERE deleted = 0 AND id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Map<String, Object>> selectIdAndRealNamesByIds(@Param("ids") List<Long> ids);

    @Select("SELECT MIN(t1.id + 1) FROM sys_user t1 WHERE NOT EXISTS (SELECT 1 FROM sys_user t2 WHERE t2.id = t1.id + 1 AND t2.deleted = 0)")
    Long selectMinAvailableId();

    @Update("UPDATE sys_user SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);
}
