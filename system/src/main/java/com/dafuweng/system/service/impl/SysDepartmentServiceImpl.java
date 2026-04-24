package com.dafuweng.system.service.impl;

import com.dafuweng.common.entity.Result;
import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.entity.SysDepartmentVO;
import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.feign.AuthUserFeignClient;
import com.dafuweng.system.service.SysDepartmentService;
import com.dafuweng.system.dao.SysDepartmentDao;
import com.dafuweng.system.dao.SysZoneDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysDepartmentServiceImpl implements SysDepartmentService {

    @Autowired
    private SysDepartmentDao sysDepartmentDao;

    @Autowired
    private SysZoneDao sysZoneDao;

    @Autowired(required = false)
    private AuthUserFeignClient authUserFeignClient;

    @Override
    public SysDepartmentEntity getById(Long id) {
        return sysDepartmentDao.selectById(id);
    }

    @Override
    public PageResponse<SysDepartmentVO> pageList(PageRequest request) {
        IPage<SysDepartmentEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件：部门名称
        if (StringUtils.hasText(request.getDeptName())) {
            wrapper.like(SysDepartmentEntity::getDeptName, request.getDeptName());
        }
        // 搜索条件：状态
        if (request.getStatus() != null) {
            wrapper.eq(SysDepartmentEntity::getStatus, request.getStatus());
        }

        // 排序
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

        // 转换为 VO 并填充关联名称
        List<SysDepartmentVO> voList = fillRelatedNames(result.getRecords());

        return PageResponse.of(result.getTotal(), voList,
            (int) page.getCurrent() , (int) page.getSize());
    }

    /**
     * 填充关联名称
     */
    private List<SysDepartmentVO> fillRelatedNames(List<SysDepartmentEntity> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 收集所有需要查询的 ID
        Set<Long> parentIds = records.stream()
            .map(SysDepartmentEntity::getParentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Set<Long> zoneIds = records.stream()
            .map(SysDepartmentEntity::getZoneId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Set<Long> managerIds = records.stream()
            .map(SysDepartmentEntity::getManagerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 2. 批量查询上级部门名称
        Map<Long, String> parentNameMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<SysDepartmentEntity> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.in(SysDepartmentEntity::getId, parentIds);
            List<SysDepartmentEntity> parentDepts = sysDepartmentDao.selectList(deptWrapper);
            parentDepts.forEach(dept -> parentNameMap.put(dept.getId(), dept.getDeptName()));
        }

        // 3. 批量查询区域名称
        Map<Long, String> zoneNameMap = new HashMap<>();
        if (!zoneIds.isEmpty()) {
            LambdaQueryWrapper<SysZoneEntity> zoneWrapper = new LambdaQueryWrapper<>();
            zoneWrapper.in(SysZoneEntity::getId, zoneIds);
            List<SysZoneEntity> zones = sysZoneDao.selectList(zoneWrapper);
            zones.forEach(zone -> zoneNameMap.put(zone.getId(), zone.getZoneName()));
        }

        // 4. 批量查询用户名称（通过 Feign 调用 auth 服务）
        Map<Long, String> managerNameMap = new HashMap<>();
        if (!managerIds.isEmpty() && authUserFeignClient != null) {
            try {
                // 调用 auth 服务的销售代表接口获取用户信息
                final Result<List<Map<String, Object>>> authResult = authUserFeignClient.listSalesReps();
                if (authResult != null && authResult.getCode() != null && authResult.getCode() == 200 && authResult.getData() != null) {
                    final Map<Long, String> tempMap = authResult.getData().stream()
                        .filter(u -> {
                            Object id = u.get("id");
                            return id != null && managerIds.contains(toLong(id));
                        })
                        .collect(Collectors.toMap(
                            u -> toLong(u.get("id")),
                            u -> (String) u.get("realName"),
                            (a, b) -> a
                        ));
                    managerNameMap.putAll(tempMap);
                }
            } catch (Exception e) {
                log.warn("调用 auth 服务获取用户信息失败: {}", e.getMessage());
            }
        }

        // 5. 填充名称（如果无法获取名称，则显示 ID）
        return records.stream().map(entity -> {
            SysDepartmentVO vo = new SysDepartmentVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setParentName(parentNameMap.get(entity.getParentId()));
            vo.setZoneName(zoneNameMap.get(entity.getZoneId()));
            // 如果无法获取用户名称，则显示 ID
            Long managerId = entity.getManagerId();
            String managerName = managerNameMap.get(managerId);
            vo.setManagerName(managerName != null ? managerName : (managerId != null ? "ID:" + managerId : null));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 将 Object 转换为 Long
     */
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
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
    public List<SysDepartmentEntity> listAll() {
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDepartmentEntity::getSortOrder);
        return sysDepartmentDao.selectList(wrapper);
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
