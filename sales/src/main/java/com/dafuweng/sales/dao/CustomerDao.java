package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.CustomerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CustomerDao extends BaseMapper<CustomerEntity> {

    List<CustomerEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);

    List<CustomerEntity> selectByStatus(@Param("status") Short status);

    List<CustomerEntity> selectCustomerToPublicSea(@Param("publicSeaDays") Integer publicSeaDays);

    @Select("SELECT COALESCE(MIN(t.id), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM customer) t WHERE NOT EXISTS (SELECT 1 FROM customer c WHERE c.id = t.id)")
    Long selectMinUnusedId();

    @Update("UPDATE customer SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
