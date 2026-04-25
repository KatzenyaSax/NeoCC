package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommissionRecordDao extends BaseMapper<CommissionRecordEntity> {

    List<CommissionRecordEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM commission_record) t WHERE NOT EXISTS (SELECT 1 FROM commission_record c WHERE c.id = t.id)")
    Long selectMinUnusedId();
}
