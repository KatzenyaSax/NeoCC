package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CustomerTransferLogDao extends BaseMapper<CustomerTransferLogEntity> {

    List<CustomerTransferLogEntity> selectByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT id FROM customer_transfer_log UNION ALL SELECT 0) t WHERE NOT EXISTS (SELECT 1 FROM customer_transfer_log c WHERE c.id = t.id + 1 AND c.deleted = 0) LIMIT 1")
    Long selectMinUnusedId();

    @Update("UPDATE customer_transfer_log SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
