package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContractEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ContractDao extends BaseMapper<ContractEntity> {

    ContractEntity selectByContractNo(@Param("contractNo") String contractNo);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM contract) t WHERE NOT EXISTS (SELECT 1 FROM contract c WHERE c.id = t.id AND c.deleted = 0)")
    Long selectMinUnusedId();

    @Update("UPDATE contract SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
