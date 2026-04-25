package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.WorkLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkLogDao extends BaseMapper<WorkLogEntity> {

    WorkLogEntity selectBySalesRepIdAndLogDate(@Param("salesRepId") Long salesRepId, @Param("logDate") String logDate);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM work_log) t WHERE NOT EXISTS (SELECT 1 FROM work_log w WHERE w.id = t.id)")
    Long selectMinUnusedId();
}
