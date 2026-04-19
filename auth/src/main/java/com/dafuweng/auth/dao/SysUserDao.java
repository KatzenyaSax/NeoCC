package com.dafuweng.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.auth.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {

    SysUserEntity selectByUsername(@Param("username") String username);

    List<SysUserEntity> selectByRoleCode(@Param("roleCode") String roleCode);
}
