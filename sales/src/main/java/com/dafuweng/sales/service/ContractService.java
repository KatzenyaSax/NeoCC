package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.dao.ContractDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

public interface ContractService {

    ContractEntity getById(Long id);

    ContractEntity getByContractNo(String contractNo);

    PageResponse<ContractEntity> pageList(PageRequest request);

    List<ContractEntity> listBySalesRepId(Long salesRepId);

    List<ContractEntity> listByStatus(Short status);

    @Transactional
    ContractEntity save(ContractEntity entity);

    @Transactional
    ContractEntity update(ContractEntity entity);

    @Transactional
    void delete(Long id);
}