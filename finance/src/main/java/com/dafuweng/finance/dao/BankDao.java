package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.BankEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BankDao extends BaseMapper<BankEntity> {

    @Select("SELECT COALESCE(MIN(t.id), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM bank) t WHERE NOT EXISTS (SELECT 1 FROM bank b WHERE b.id = t.id)")
    Long selectMinUnusedId();

    @Update("UPDATE bank SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
