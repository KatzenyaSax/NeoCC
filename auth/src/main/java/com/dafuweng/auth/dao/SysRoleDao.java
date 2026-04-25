package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysRoleDao extends BaseMapper<SysRoleEntity> {

    @Select("<script>SELECT id, role_name FROM sys_role WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Map<String, Object>> selectIdAndRoleNamesByIds(@Param("ids") List<Long> ids);

    @Select("SELECT MIN(t1.id + 1) FROM sys_role t1 WHERE NOT EXISTS (SELECT 1 FROM sys_role t2 WHERE t2.id = t1.id + 1 AND t2.deleted = 0)")
    Long selectMinUnusedId();

    @Update("UPDATE sys_role SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
