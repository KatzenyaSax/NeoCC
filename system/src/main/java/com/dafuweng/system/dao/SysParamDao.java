package com.dafuweng.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.system.entity.SysParamEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysParamDao extends BaseMapper<SysParamEntity> {

    SysParamEntity selectByParamKey(@Param("paramKey") String paramKey);
}
