package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerTransferLogDao extends BaseMapper<CustomerTransferLogEntity> {

    List<CustomerTransferLogEntity> selectByCustomerId(@Param("customerId") Long customerId);
}
