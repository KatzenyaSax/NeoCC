package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.service.ContactRecordService;
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

@Service
public class ContactRecordServiceImpl implements ContactRecordService {

    @Autowired
    private ContactRecordDao contactRecordDao;

    @Override
    public ContactRecordEntity getById(Long id) {
        return contactRecordDao.selectById(id);
    }

    @Override
    public PageResponse<ContactRecordEntity> pageList(PageRequest request) {
        IPage<ContactRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ContactRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ContactRecordEntity::getId);
            } else {
                wrapper.orderByDesc(ContactRecordEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ContactRecordEntity::getCreatedAt);
        }
        IPage<ContactRecordEntity> result = contactRecordDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ContactRecordEntity> listByCustomerId(Long customerId) {
        return contactRecordDao.selectByCustomerId(customerId);
    }

    @Override
    public List<ContactRecordEntity> listBySalesRepId(Long salesRepId) {
        return contactRecordDao.selectBySalesRepId(salesRepId);
    }

    @Override
    public List<ContactRecordEntity> listBySalesRepIds(List<Long> salesRepIds) {
        if (salesRepIds == null || salesRepIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        LambdaQueryWrapper<ContactRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ContactRecordEntity::getSalesRepId, salesRepIds);
        wrapper.orderByDesc(ContactRecordEntity::getContactDate);
        return contactRecordDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public ContactRecordEntity save(ContactRecordEntity entity) {
        contactRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ContactRecordEntity update(ContactRecordEntity entity) {
        contactRecordDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contactRecordDao.deleteById(id);
    }
}