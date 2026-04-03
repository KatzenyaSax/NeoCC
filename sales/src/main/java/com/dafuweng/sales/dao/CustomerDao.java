package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.CustomerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerDao extends BaseMapper<CustomerEntity> {

    List<CustomerEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);

    List<CustomerEntity> selectByStatus(@Param("status") Short status);
}
