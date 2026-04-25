package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.service.SysZoneService;
import com.dafuweng.system.dao.SysZoneDao;
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
public class SysZoneServiceImpl implements SysZoneService {

    @Autowired
    private SysZoneDao sysZoneDao;

    @Autowired
    private com.dafuweng.system.feign.AuthFeignClient authFeignClient;

    @Override
    public SysZoneEntity getById(Long id) {
        return sysZoneDao.selectById(id);
    }

    @Override
    public PageResponse<SysZoneEntity> pageList(PageRequest request) {
        IPage<SysZoneEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
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
        fillDirectorNames(result.getRecords());
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysZoneEntity> listAll() {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        List<SysZoneEntity> zones = sysZoneDao.selectList(wrapper);
        fillDirectorNames(zones);
        return zones;
    }

    @Override
    public List<SysZoneEntity> listByStatus(Short status) {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysZoneEntity::getStatus, status);
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        List<SysZoneEntity> zones = sysZoneDao.selectList(wrapper);
        fillDirectorNames(zones);
        return zones;
    }

    @Override
    @Transactional
    public SysZoneEntity save(SysZoneEntity entity) {
        sysZoneDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysZoneEntity update(SysZoneEntity entity) {
        sysZoneDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysZoneDao.deleteById(id);
    }

    @Override
    public Map<Long, String> listNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysZoneEntity::getId, ids);
        List<SysZoneEntity> zones = sysZoneDao.selectList(wrapper);
        return zones.stream().collect(Collectors.toMap(SysZoneEntity::getId, SysZoneEntity::getZoneName));
    }

    @Override
    public void fillDirectorNames(List<SysZoneEntity> zones) {
        if (zones == null || zones.isEmpty()) {
            return;
        }
        // 收集所有 directorId
        List<Long> directorIds = zones.stream()
            .map(SysZoneEntity::getDirectorId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        if (directorIds.isEmpty()) {
            return;
        }

        // 通过 Feign 调用 auth 模块查询用户姓名
        Result<Map<Long, String>> result = authFeignClient.listUserNamesByIds(directorIds);
        Map<Long, String> nameMap = result.getData();
        if (nameMap == null) {
            return; // 安全返回
        }

        // 设置姓名到对应战区
        zones.forEach(zone -> {
            if (zone.getDirectorId() != null && nameMap != null) {
                zone.setDirectorName(nameMap.get(zone.getDirectorId()));
            }
        });
    }

    @Override
    public Long getMinUnusedId() {
        Long minId = sysZoneDao.selectMinUnusedId();
        return minId != null ? minId : 1L;
    }
}
