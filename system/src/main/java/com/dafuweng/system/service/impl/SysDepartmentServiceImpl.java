package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.service.SysDepartmentService;
import com.dafuweng.system.service.SysZoneService;
import com.dafuweng.system.dao.SysDepartmentDao;
import com.dafuweng.system.dao.SysZoneDao;
import com.dafuweng.system.feign.AuthFeignClient;
import com.dafuweng.system.vo.DeptVO;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class SysDepartmentServiceImpl implements SysDepartmentService {

    @Autowired
    private SysDepartmentDao sysDepartmentDao;

    @Autowired
    private SysZoneService sysZoneService;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Override
    public SysDepartmentEntity getById(Long id) {
        return sysDepartmentDao.selectById(id);
    }

    @Override
    public PageResponse<SysDepartmentEntity> pageList(PageRequest request) {
        IPage<SysDepartmentEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysDepartmentEntity::getId);
            } else {
                wrapper.orderByDesc(SysDepartmentEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysDepartmentEntity::getCreatedAt);
        }
        IPage<SysDepartmentEntity> result = sysDepartmentDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public PageResponse<DeptVO> pageListWithDetails(PageRequest request) {
        // 1. 查询部门分页数据
        IPage<SysDepartmentEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortField())) {
                wrapper.orderByAsc(SysDepartmentEntity::getId);
            } else {
                wrapper.orderByDesc(SysDepartmentEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysDepartmentEntity::getCreatedAt);
        }
        IPage<SysDepartmentEntity> result = sysDepartmentDao.selectPage(page, wrapper);
        List<SysDepartmentEntity> depts = result.getRecords();

        // 2. 提取 parentIds、zoneIds、managerIds
        List<Long> parentIds = depts.stream()
                .map(SysDepartmentEntity::getParentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> zoneIds = depts.stream()
                .map(SysDepartmentEntity::getZoneId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> managerIds = depts.stream()
                .map(SysDepartmentEntity::getManagerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 3. 查询上级部门名称
        Map<Long, String> parentDeptMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            parentDeptMap.putAll(listNamesByIds(parentIds));
        }

        // 4. 查询战区名称
        Map<Long, String> zoneMap = new HashMap<>();
        if (!zoneIds.isEmpty()) {
            zoneMap.putAll(sysZoneService.listNamesByIds(zoneIds));
        }

        // 5. 查询负责人姓名（通过feign调用auth）
        Map<Long, String> managerMap = new HashMap<>();
        if (!managerIds.isEmpty()) {
            Result<Map<Long, String>> result2 = authFeignClient.listUserNamesByIds(managerIds);
            if (result2 != null && result2.getData() != null) {
                managerMap.putAll(result2.getData());
            }
        }

        // 6. 组装DeptVO
        List<DeptVO> voList = depts.stream().map(dept -> {
            DeptVO vo = new DeptVO();
            vo.setId(dept.getId());
            vo.setDeptCode(dept.getDeptCode());
            vo.setDeptName(dept.getDeptName());
            vo.setParentId(dept.getParentId());
            vo.setZoneId(dept.getZoneId());
            vo.setManagerId(dept.getManagerId());
            vo.setSortOrder(dept.getSortOrder());
            vo.setStatus(dept.getStatus());
            vo.setCreatedBy(dept.getCreatedBy());
            vo.setCreatedAt(dept.getCreatedAt());
            vo.setUpdatedBy(dept.getUpdatedBy());
            vo.setUpdatedAt(dept.getUpdatedAt());
            vo.setParentDeptName(parentDeptMap.get(dept.getParentId()));
            vo.setZoneName(zoneMap.get(dept.getZoneId()));
            vo.setManagerName(managerMap.get(dept.getManagerId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResponse.of(result.getTotal(), voList, (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public List<SysDepartmentEntity> listByParentId(Long parentId) {
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartmentEntity::getParentId, parentId);
        wrapper.orderByAsc(SysDepartmentEntity::getSortOrder);
        return sysDepartmentDao.selectList(wrapper);
    }

    @Override
    public List<SysDepartmentEntity> listByZoneId(Long zoneId) {
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartmentEntity::getZoneId, zoneId);
        wrapper.orderByAsc(SysDepartmentEntity::getSortOrder);
        return sysDepartmentDao.selectList(wrapper);
    }

    @Override
    public Map<Long, String> listNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysDepartmentEntity::getId, ids);
        List<SysDepartmentEntity> depts = sysDepartmentDao.selectList(wrapper);
        return depts.stream().collect(java.util.stream.Collectors.toMap(SysDepartmentEntity::getId, SysDepartmentEntity::getDeptName));
    }

    @Override
    @Transactional
    public SysDepartmentEntity save(SysDepartmentEntity entity) {
        sysDepartmentDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysDepartmentEntity update(SysDepartmentEntity entity) {
        sysDepartmentDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysDepartmentDao.deleteById(id);
    }
}
