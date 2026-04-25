package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.dao.ContactRecordDao;
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

public interface ContactRecordService {

    ContactRecordEntity getById(Long id);

    PageResponse<ContactRecordEntity> pageList(PageRequest request);

    List<ContactRecordEntity> listByCustomerId(Long customerId);

    List<ContactRecordEntity> listBySalesRepId(Long salesRepId);

    List<ContactRecordEntity> listBySalesRepIds(List<Long> salesRepIds);

    @Transactional
    ContactRecordEntity save(ContactRecordEntity entity);

    @Transactional
    ContactRecordEntity update(ContactRecordEntity entity);

    @Transactional
    void delete(Long id);

    Long getMinUnusedId();
}