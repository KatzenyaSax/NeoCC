package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.FinanceProductEntity;
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

public interface FinanceProductService {

    FinanceProductEntity getById(Long id);

    PageResponse<FinanceProductEntity> pageList(PageRequest request);

    List<FinanceProductEntity> listByBankId(Long bankId);

    List<FinanceProductEntity> listByStatus(Short status);

    @Transactional
    FinanceProductEntity save(FinanceProductEntity entity);

    @Transactional
    FinanceProductEntity update(FinanceProductEntity entity);

    @Transactional
    void delete(Long id);
}
