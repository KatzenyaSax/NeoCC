package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.service.ContactRecordService;
import com.dafuweng.sales.dao.ContactRecordDao;
import com.dafuweng.sales.dao.CustomerDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContactRecordServiceImpl implements ContactRecordService {

    @Autowired
    private ContactRecordDao contactRecordDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AuthFeignClient authFeignClient;

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

        // 关联查询客户名称和销售代表名称
        List<ContactRecordEntity> records = result.getRecords();
        if (!records.isEmpty()) {
            // 收集所有客户ID
            List<Long> customerIds = records.stream()
                .map(ContactRecordEntity::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

            // 批量查询客户信息
            Map<Long, String> customerNameMap = customerDao.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(CustomerEntity::getId, CustomerEntity::getName, (a, b) -> a));

            // 收集所有销售代表ID
            List<Long> salesRepIds = records.stream()
                .map(ContactRecordEntity::getSalesRepId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

            // 批量查询销售代表名称
            Map<Long, String> salesRepNameMap = new HashMap<>();
            if (!salesRepIds.isEmpty()) {
                Result<List<Map<String, Object>>> repResult = authFeignClient.listUsersByIds(salesRepIds);
                if (repResult != null && repResult.getData() != null) {
                    for (Map<String, Object> user : repResult.getData()) {
                        Long id = ((Number) user.get("id")).longValue();
                        String realName = (String) user.get("realName");
                        salesRepNameMap.put(id, realName != null ? realName : (String) user.get("username"));
                    }
                }
            }

            // 设置客户名称和销售代表名称
            records.forEach(r -> {
                r.setCustomerName(customerNameMap.get(r.getCustomerId()));
                if (r.getSalesRepId() != null) {
                    r.setSalesRepName(salesRepNameMap.get(r.getSalesRepId()));
                }
            });
        }

        return PageResponse.of(result.getTotal(), records,
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