package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.WorkLogEntity;
import com.dafuweng.sales.dao.WorkLogDao;
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

public interface WorkLogService {

    WorkLogEntity getById(Long id);

    PageResponse<WorkLogEntity> pageList(PageRequest request);

    List<WorkLogEntity> listBySalesRepId(Long salesRepId);

    boolean isDuplicate(Long salesRepId, String logDate);

    @Transactional
    WorkLogEntity save(WorkLogEntity entity);

    @Transactional
    WorkLogEntity update(WorkLogEntity entity);

    @Transactional
    void delete(Long id);
}