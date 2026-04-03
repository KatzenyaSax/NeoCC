package com.dafuweng.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.system.entity.SysOperationLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysOperationLogDao extends BaseMapper<SysOperationLogEntity> {

    /**
     * 分页查询操作日志
     * @param current 当前页
     * @param size 每页大小
     * @return 操作日志列表
     */
    List<SysOperationLogEntity> selectPage(@Param("current") Long current, @Param("size") Long size);
}
