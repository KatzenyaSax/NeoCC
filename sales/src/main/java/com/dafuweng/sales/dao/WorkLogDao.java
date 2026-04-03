package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.WorkLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkLogDao extends BaseMapper<WorkLogEntity> {

    WorkLogEntity selectBySalesRepIdAndLogDate(@Param("salesRepId") Long salesRepId, @Param("logDate") String logDate);
}
