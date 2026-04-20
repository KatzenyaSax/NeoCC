package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.service.ContractService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractDao contractDao;

    @Override
    public ContractEntity getById(Long id) {
        return contractDao.selectById(id);
    }

    @Override
    public ContractEntity getByContractNo(String contractNo) {
        return contractDao.selectByContractNo(contractNo);
    }

    @Override
    public PageResponse<ContractEntity> pageList(PageRequest request) {
        IPage<ContractEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ContractEntity::getId);
            } else {
                wrapper.orderByDesc(ContractEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ContractEntity::getCreatedAt);
        }
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ContractEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getSalesRepId, salesRepId);
        return contractDao.selectList(wrapper);
    }

    @Override
    public List<ContractEntity> listByStatus(Short status) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getStatus, status);
        return contractDao.selectList(wrapper);
    }

    @Override
    public PageResponse<ContractEntity> pageListByStatus(int pageNum, int pageSize, Short status) {
        IPage<ContractEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ContractEntity::getStatus, status);
        }

        wrapper.orderByDesc(ContractEntity::getCreatedAt);
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional
    public ContractEntity save(ContractEntity entity) {
        contractDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ContractEntity update(ContractEntity entity) {
        contractDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractDao.deleteById(id);
    }

    @Override
    public String generateContractNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HT-" + dateStr + "-";
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ContractEntity::getContractNo, prefix)
               .orderByDesc(ContractEntity::getId)
               .last("LIMIT 1");
        ContractEntity lastContract = contractDao.selectOne(wrapper);
        int sequence = 1;
        if (lastContract != null && lastContract.getContractNo() != null) {
            String lastNo = lastContract.getContractNo();
            String lastSeqStr = lastNo.substring(lastNo.lastIndexOf("-") + 1);
            try {
                sequence = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%04d", sequence);
    }

    @Override
    public ContractEntity getDetail(Long id) {
        return contractDao.selectById(id);
    }
}