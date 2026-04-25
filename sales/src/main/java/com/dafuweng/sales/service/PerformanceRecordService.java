package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.dao.PerformanceRecordDao;
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

public interface PerformanceRecordService {

    PerformanceRecordEntity getById(Long id);

    PageResponse<PerformanceRecordEntity> pageList(PageRequest request);

    List<PerformanceRecordEntity> listBySalesRepId(Long salesRepId);

    @Transactional
    PerformanceRecordEntity save(PerformanceRecordEntity entity);

    @Transactional
    PerformanceRecordEntity update(PerformanceRecordEntity entity);

    @Transactional
    void delete(Long id);

    Long getMinUnusedId();

    PerformanceRecordEntity getByContractId(Long contractId);
}