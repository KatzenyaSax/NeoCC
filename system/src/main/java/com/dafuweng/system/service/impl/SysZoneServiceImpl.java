package com.dafuweng.system.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.service.SysZoneService;
import com.dafuweng.system.dao.SysZoneDao;
import com.dafuweng.system.feign.AuthUserFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysZoneServiceImpl implements SysZoneService {

    @Autowired
    private SysZoneDao sysZoneDao;

    @Autowired(required = false)
    private AuthUserFeignClient authUserFeignClient;

    /**
     * 转换Long类型（兼容不同类型）
     */
    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof String && StringUtils.hasText((String) obj)) {
            return Long.parseLong((String) obj);
        }
        return null;
    }

    /**
     * 填充负责人名称
     */
    private void fillDirectorName(List<SysZoneEntity> records) {
        if (records == null || records.isEmpty()) return;

        // 1. 收集所有 directorId
        Set<Long> directorIds = records.stream()
            .map(SysZoneEntity::getDirectorId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (directorIds.isEmpty()) return;

        // 2. 调用 auth 服务获取用户信息
        Map<Long, String> directorNameMap = new HashMap<>();
        if (authUserFeignClient != null) {
            try {
                Result<List<Map<String, Object>>> authResult = authUserFeignClient.listSalesReps();
                if (authResult != null && authResult.getCode() != null && authResult.getCode() == 200 && authResult.getData() != null) {
                    directorNameMap = authResult.getData().stream()
                        .filter(u -> {
                            Object id = u.get("id");
                            return id != null && directorIds.contains(toLong(id));
                        })
                        .collect(Collectors.toMap(
                            u -> toLong(u.get("id")),
                            u -> (String) u.get("realName"),
                            (a, b) -> a
                        ));
                }
            } catch (Exception e) {
                log.warn("调用 auth 服务获取负责人信息失败: {}", e.getMessage());
            }
        }

        // 3. 填充 directorName（如果无法获取名称，则显示 ID）
        for (SysZoneEntity zone : records) {
            Long directorId = zone.getDirectorId();
            if (directorId != null) {
                String name = directorNameMap.get(directorId);
                zone.setDirectorName(name != null ? name : "ID:" + directorId);
            }
        }
    }

    @Override
    public SysZoneEntity getById(Long id) {
        SysZoneEntity entity = sysZoneDao.selectById(id);
        if (entity != null) {
            fillDirectorName(Collections.singletonList(entity));
        }
        return entity;
    }

    @Override
    public PageResponse<SysZoneEntity> pageList(PageRequest request) {
        IPage<SysZoneEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件：区域名称
        if (StringUtils.hasText(request.getZoneName())) {
            wrapper.like(SysZoneEntity::getZoneName, request.getZoneName());
        }
        // 搜索条件：状态
        if (request.getStatus() != null) {
            wrapper.eq(SysZoneEntity::getStatus, request.getStatus());
        }

        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysZoneEntity::getId);
            } else {
                wrapper.orderByDesc(SysZoneEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysZoneEntity::getCreatedAt);
        }
        IPage<SysZoneEntity> result = sysZoneDao.selectPage(page, wrapper);
        // 填充负责人名称
        fillDirectorName(result.getRecords());
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysZoneEntity> listAll() {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        List<SysZoneEntity> records = sysZoneDao.selectList(wrapper);
        // 填充负责人名称
        fillDirectorName(records);
        return records;
    }

    @Override
    public List<SysZoneEntity> listByStatus(Short status) {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysZoneEntity::getStatus, status);
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        List<SysZoneEntity> records = sysZoneDao.selectList(wrapper);
        // 填充负责人名称
        fillDirectorName(records);
        return records;
    }

    @Override
    @Transactional
    public SysZoneEntity save(SysZoneEntity entity) {
        sysZoneDao.insert(entity);
        // 填充负责人名称
        fillDirectorName(Collections.singletonList(entity));
        return entity;
    }

    @Override
    @Transactional
    public SysZoneEntity update(SysZoneEntity entity) {
        sysZoneDao.updateById(entity);
        // 填充负责人名称
        fillDirectorName(Collections.singletonList(entity));
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysZoneDao.deleteById(id);
    }
}
