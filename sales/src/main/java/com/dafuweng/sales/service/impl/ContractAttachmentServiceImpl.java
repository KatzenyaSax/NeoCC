package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractAttachmentEntity;
import com.dafuweng.sales.service.ContractAttachmentService;
import com.dafuweng.sales.dao.ContractAttachmentDao;
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
public class ContractAttachmentServiceImpl implements ContractAttachmentService {

    @Autowired
    private ContractAttachmentDao contractAttachmentDao;

    @Override
    public ContractAttachmentEntity getById(Long id) {
        return contractAttachmentDao.selectById(id);
    }

    @Override
    public PageResponse<ContractAttachmentEntity> pageList(PageRequest request) {
        IPage<ContractAttachmentEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ContractAttachmentEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ContractAttachmentEntity::getId);
            } else {
                wrapper.orderByDesc(ContractAttachmentEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ContractAttachmentEntity::getId);
        }
        IPage<ContractAttachmentEntity> result = contractAttachmentDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ContractAttachmentEntity> listByContractId(Long contractId) {
        return contractAttachmentDao.selectByContractId(contractId);
    }

    @Override
    @Transactional
    public ContractAttachmentEntity save(ContractAttachmentEntity entity) {
        contractAttachmentDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ContractAttachmentEntity update(ContractAttachmentEntity entity) {
        contractAttachmentDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractAttachmentDao.deleteById(id);
    }

    @Override
    public Long getMinUnusedId() {
        return contractAttachmentDao.selectMinUnusedId();
    }
}