package com.dafuweng.auth.service.impl;

import com.dafuweng.auth.dao.SysPermissionDao;
import com.dafuweng.auth.dao.SysRolePermissionDao;
import com.dafuweng.auth.dao.SysRoleDao;
import com.dafuweng.auth.dao.SysUserDao;
import com.dafuweng.auth.dao.SysUserRoleDao;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.entity.SysUserRoleEntity;
import com.dafuweng.auth.feign.SystemFeignClient;
import com.dafuweng.auth.vo.UserVO;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.dafuweng.auth.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Autowired
    private SysRoleDao sysRoleDao;

    @Autowired
    private SysRolePermissionDao sysRolePermissionDao;

    @Autowired
    private SysPermissionDao sysPermissionDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SystemFeignClient systemFeignClient;

    @Override
    public SysUserEntity getById(Long id) {
        SysUserEntity user = sysUserDao.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public PageResponse<SysUserEntity> pageList(PageRequest request) {
        IPage<SysUserEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserEntity::getDeleted, (short) 0);
        applySort(wrapper, request);
        IPage<SysUserEntity> result = sysUserDao.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    private void applySort(LambdaQueryWrapper<SysUserEntity> wrapper, PageRequest request) {
        if (!StringUtils.hasText(request.getSortField())) {
            wrapper.orderByDesc(SysUserEntity::getId);
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(request.getSortOrder());
        switch (request.getSortField()) {
            case "id":
                if (asc) wrapper.orderByAsc(SysUserEntity::getId);
                else wrapper.orderByDesc(SysUserEntity::getId);
                break;
            case "username":
                if (asc) wrapper.orderByAsc(SysUserEntity::getUsername);
                else wrapper.orderByDesc(SysUserEntity::getUsername);
                break;
            case "realName":
                if (asc) wrapper.orderByAsc(SysUserEntity::getRealName);
                else wrapper.orderByDesc(SysUserEntity::getRealName);
                break;
            case "phone":
                if (asc) wrapper.orderByAsc(SysUserEntity::getPhone);
                else wrapper.orderByDesc(SysUserEntity::getPhone);
                break;
            case "email":
                if (asc) wrapper.orderByAsc(SysUserEntity::getEmail);
                else wrapper.orderByDesc(SysUserEntity::getEmail);
                break;
            case "status":
                if (asc) wrapper.orderByAsc(SysUserEntity::getStatus);
                else wrapper.orderByDesc(SysUserEntity::getStatus);
                break;
            case "createdAt":
                if (asc) wrapper.orderByAsc(SysUserEntity::getCreatedAt);
                else wrapper.orderByDesc(SysUserEntity::getCreatedAt);
                break;
            default:
                wrapper.orderByDesc(SysUserEntity::getId);
        }
    }

    @Override
    public PageResponse<UserVO> pageListWithDeptName(PageRequest request) {
        // 1. query user page data
        IPage<SysUserEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserEntity::getDeleted, (short) 0);
        applySort(wrapper, request);
        IPage<SysUserEntity> result = sysUserDao.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        List<SysUserEntity> users = result.getRecords();

        // 2. extract deptIds
        List<Long> deptIds = users.stream()
                .map(SysUserEntity::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 3. extract userIds
        List<Long> userIds = users.stream()
                .map(SysUserEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 4. query dept names from system
        Map<Long, String> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            Result<Map<Long, String>> deptResult = systemFeignClient.listDeptNamesByIds(deptIds);
            if (deptResult != null && deptResult.getData() != null) {
                deptMap.putAll(deptResult.getData());
            }
        }

        // 5. query user-role mappings and role names
        Map<Long, String> roleNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Map<String, Object>> userRoleMappings = sysUserRoleDao.selectUserRoleMappings(userIds);
            List<Long> allRoleIds = userRoleMappings.stream()
                    .map(m -> ((Number) m.get("role_id")).longValue())
                    .distinct()
                    .collect(Collectors.toList());
            if (!allRoleIds.isEmpty()) {
                List<Map<String, Object>> roleIdNames = sysRoleDao.selectIdAndRoleNamesByIds(allRoleIds);
                Map<Long, String> roleIdNameMap = new HashMap<>();
                for (Map<String, Object> r : roleIdNames) {
                    Long roleId = ((Number) r.get("id")).longValue();
                    String roleName = (String) r.get("role_name");
                    roleIdNameMap.put(roleId, roleName);
                }
                Map<Long, List<String>> userRoleNames = new HashMap<>();
                for (Map<String, Object> m : userRoleMappings) {
                    Long userId = ((Number) m.get("user_id")).longValue();
                    Long roleId = ((Number) m.get("role_id")).longValue();
                    String roleName = roleIdNameMap.get(roleId);
                    if (roleName != null) {
                        userRoleNames.computeIfAbsent(userId, k -> new ArrayList<>()).add(roleName);
                    }
                }
                for (Map.Entry<Long, List<String>> entry : userRoleNames.entrySet()) {
                    roleNameMap.put(entry.getKey(), String.join(",", entry.getValue()));
                }
            }
        }

        // 6. convert to UserVO
        List<UserVO> voList = users.stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setDeptId(user.getDeptId());
            vo.setZoneId(user.getZoneId());
            vo.setStatus(user.getStatus());
            vo.setLoginErrorCount(user.getLoginErrorCount());
            vo.setLockTime(user.getLockTime());
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setLastLoginIp(user.getLastLoginIp());
            vo.setCreatedBy(user.getCreatedBy());
            vo.setCreatedAt(user.getCreatedAt());
            vo.setUpdatedBy(user.getUpdatedBy());
            vo.setUpdatedAt(user.getUpdatedAt());
            vo.setDeptName(deptMap.get(user.getDeptId()));
            vo.setRoleName(roleNameMap.get(user.getId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResponse.of(result.getTotal(), voList,
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public SysUserEntity getByUsername(String username) {
        return sysUserDao.selectByUsername(username);
    }

    @Override
    @Transactional
    public SysUserEntity login(String username, String password, String loginIp) {
        SysUserEntity user = sysUserDao.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String passwordHash = user.getPassword();
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            throw new IllegalArgumentException("账号已锁定，请稍后再试");
        }
        if (Objects.equals(user.getDeleted(), (short) 1)) {
            throw new IllegalArgumentException("账号已删除");
        }
        if (!passwordEncoder.matches(password, passwordHash)) {
            int errors = (user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount()) + 1;
            user.setLoginErrorCount(errors);
            if (errors >= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 30);
                user.setLockTime(cal.getTime());
            }
            sysUserDao.updateById(user);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        sysUserDao.update(null,
                new LambdaUpdateWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getId, user.getId())
                        .set(SysUserEntity::getLoginErrorCount, 0)
                        .set(SysUserEntity::getLastLoginTime, new Date())
                        .set(SysUserEntity::getLastLoginIp, loginIp));
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(null);
            user.setLastLoginIp(null);
            sysUserDao.updateById(user);
        }
    }

    @Override
    @Transactional
    public void unlock(Long userId) {
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setLockTime(null);
        user.setLoginErrorCount(0);
        sysUserDao.updateById(user);
    }

    @Override
    public List<String> getPermCodesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> allPermCodes = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<String> codes = sysPermissionDao.selectPermCodesByRoleId(roleId);
            if (codes != null) {
                allPermCodes.addAll(codes);
            }
        }
        return allPermCodes;
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        return roleIds == null ? new ArrayList<>() : roleIds;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleDao.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRoleEntity> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setCreatedAt(new Date());
                userRoles.add(ur);
            }
            sysUserRoleDao.insertBatch(userRoles);
        }
    }

    @Override
    @Transactional
    public SysUserEntity save(SysUserEntity entity) {
        entity.setDeleted((short) 0);
        sysUserDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysUserEntity update(SysUserEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            // Soft delete - use custom method to SET deleted=1 instead of filtering by deleted=0
            sysUserDao.softDeleteById(entity.getId());
        } else {
            // Normal update
            sysUserDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysUserDao.deleteById(id);
    }

    @Override
    public List<SysUserEntity> listSalesReps() {
        List<SysUserEntity> list = sysUserDao.selectByRoleCode("sales_rep");
        list.forEach(u -> u.setPassword(null));
        return list;
    }

    @Override
    public List<SysUserEntity> listSalesReps(Long zoneId, Long deptId, Long salesRepId) {
        // 先获取所有销售代表
        List<SysUserEntity> allSalesReps = sysUserDao.selectByRoleCode("sales_rep");

        // 根据条件过滤
        List<SysUserEntity> filteredReps = allSalesReps.stream()
                .filter(user -> {
                    // 如果有zoneId条件，只保留匹配的用户
                    if (zoneId != null) {
                        return Objects.equals(user.getZoneId(), zoneId);
                    }
                    // 如果有deptId条件，只保留匹配的用户
                    if (deptId != null) {
                        return Objects.equals(user.getDeptId(), deptId);
                    }
                    // 如果有salesRepId条件，只保留匹配的用户
                    if (salesRepId != null) {
                        return Objects.equals(user.getId(), salesRepId);
                    }
                    // 没有条件时返回所有
                    return true;
                })
                .collect(Collectors.toList());

        filteredReps.forEach(u -> u.setPassword(null));
        return filteredReps;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserDao.updateById(user);
        return true;
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        LambdaUpdateWrapper<SysUserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUserEntity::getId, userId)
               .set(SysUserEntity::getPassword, passwordEncoder.encode(newPassword));
        sysUserDao.update(null, wrapper);
    }

    @Override
    public Long count() {
        return sysUserDao.selectCount(null);
    }

    @Override
    public List<Long> listUserIdsByDeptId(Long deptId) {
        return sysUserDao.selectUserIdsByDeptId(deptId);
    }

    @Override
    public List<Long> listUserIdsByZoneId(Long zoneId) {
        return sysUserDao.selectUserIdsByZoneId(zoneId);
    }

    @Override
    public Map<Long, String> listRealNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        List<Map<String, Object>> results = sysUserDao.selectIdAndRealNamesByIds(ids);
        Map<Long, String> map = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long id = ((Number) row.get("id")).longValue();
            String realName = (String) row.get("real_name");
            map.put(id, realName);
        }
        return map;
    }

    @Override
    public List<SysUserEntity> listByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = sysUserRoleDao.selectUserIdsByRoleIds(roleIds);
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysUserEntity> users = sysUserDao.selectBatchIds(userIds);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public Long selectMinAvailableId() {
        Long minId = sysUserDao.selectMinAvailableId();
        return minId != null ? minId : 1L;
    }
}
