package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysRoleDao extends BaseMapper<SysRoleEntity> {

    @Select("<script>SELECT id, role_name FROM sys_role WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Map<String, Object>> selectIdAndRoleNamesByIds(@Param("ids") List<Long> ids);
}
