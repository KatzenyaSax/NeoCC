package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.BankEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankDao extends BaseMapper<BankEntity> {
}
