package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerTransferLogDao extends BaseMapper<CustomerTransferLogEntity> {

    List<CustomerTransferLogEntity> selectByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM customer_transfer_log) t WHERE NOT EXISTS (SELECT 1 FROM customer_transfer_log c WHERE c.id = t.id)")
    Long selectMinUnusedId();
}
