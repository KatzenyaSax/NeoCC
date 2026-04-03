package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.dao.CustomerTransferLogDao;
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

public interface CustomerTransferLogService {

    CustomerTransferLogEntity getById(Long id);

    PageResponse<CustomerTransferLogEntity> pageList(PageRequest request);

    List<CustomerTransferLogEntity> listByCustomerId(Long customerId);

    @Transactional
    CustomerTransferLogEntity save(CustomerTransferLogEntity entity);

    @Transactional
    CustomerTransferLogEntity update(CustomerTransferLogEntity entity);

    @Transactional
    void delete(Long id);
}