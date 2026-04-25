package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PerformanceRecordDao extends BaseMapper<PerformanceRecordEntity> {

    PerformanceRecordEntity selectByContractId(@Param("contractId") Long contractId);

    PerformanceRecordEntity selectOne(Wrapper<PerformanceRecordEntity> wrapper);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM performance_record) t WHERE NOT EXISTS (SELECT 1 FROM performance_record c WHERE c.id = t.id)")
    Long selectMinUnusedId();
}
