package com.dafuweng.auth.service.impl;

import com.dafuweng.auth.dao.SysPermissionDao;
import com.dafuweng.auth.dao.SysRolePermissionDao;
import com.dafuweng.auth.dao.SysUserDao;
import com.dafuweng.auth.dao.SysUserRoleDao;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.entity.SysUserVO;
import com.dafuweng.auth.entity.SysUserRoleEntity;
import com.dafuweng.auth.feign.DepartmentFeignClient;
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

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SysUserServiceImpl implements SysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Autowired
    private SysRolePermissionDao sysRolePermissionDao;

    @Autowired
    private SysPermissionDao sysPermissionDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    @Override
    public SysUserEntity getById(Long id) {
        SysUserEntity user = sysUserDao.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public PageResponse<SysUserVO> pageList(PageRequest request) {
        IPage<SysUserEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件：用户名
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysUserEntity::getUsername, request.getUsername());
        }
        // 搜索条件：真实姓名
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(SysUserEntity::getRealName, request.getRealName());
        }
        // 搜索条件：状态
        if (request.getStatus() != null) {
            wrapper.eq(SysUserEntity::getStatus, request.getStatus());
        }

        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysUserEntity::getId);
            } else {
                wrapper.orderByDesc(SysUserEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysUserEntity::getCreatedAt);
        }
        IPage<SysUserEntity> result = sysUserDao.selectPage(page, wrapper);

        // 填充部门名称
        List<SysUserVO> voList = fillDeptName(result.getRecords());

        return PageResponse.of(result.getTotal(), voList,
            (int) page.getCurrent(), (int) page.getSize());
    }
    
    /**
     * 填充用户列表的部门名称
     */
    private List<SysUserVO> fillDeptName(List<SysUserEntity> entities) {
        // 获取部门映射
        Map<Long, String> deptMap = getDeptNameMap();
        
        return entities.stream().map(entity -> {
            SysUserVO vo = new SysUserVO();
            vo.setId(entity.getId());
            vo.setUsername(entity.getUsername());
            vo.setRealName(entity.getRealName());
            vo.setPhone(entity.getPhone());
            vo.setEmail(entity.getEmail());
            vo.setDeptId(entity.getDeptId());
            vo.setZoneId(entity.getZoneId());
            vo.setStatus(entity.getStatus());
            vo.setLoginErrorCount(entity.getLoginErrorCount());
            vo.setLockTime(entity.getLockTime());
            vo.setLastLoginTime(entity.getLastLoginTime());
            vo.setLastLoginIp(entity.getLastLoginIp());
            vo.setCreatedAt(entity.getCreatedAt());
            vo.setUpdatedAt(entity.getUpdatedAt());
            // 填充部门名称
            vo.setDeptName(deptMap.get(entity.getDeptId()));
            // 不返回密码
            return vo;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取部门ID到名称的映射
     */
    private Map<Long, String> getDeptNameMap() {
        Map<Long, String> deptMap = new HashMap<>();
        try {
            Result<List<Map<String, Object>>> result = departmentFeignClient.listAll();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                for (Map<String, Object> dept : result.getData()) {
                    Object idObj = dept.get("id");
                    Object nameObj = dept.get("deptName");
                    if (idObj != null && nameObj != null) {
                        Long id = ((Number) idObj).longValue();
                        String name = String.valueOf(nameObj);
                        deptMap.put(id, name);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取部门列表失败: {}", e.getMessage());
        }
        return deptMap;
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
        // 保存密码 hash（登录成功后要清除，避免返回给前端）
        String passwordHash = user.getPassword();
        // 检查账号锁定
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            throw new IllegalArgumentException("账号已锁定，请稍后再试");
        }
        // 检查删除状态
        if (Objects.equals(user.getDeleted(), (short) 1)) {
            throw new IllegalArgumentException("账号已删除");
        }
        // 验证密码（BCrypt）
        if (!passwordEncoder.matches(password, passwordHash)) {
            int errors = (user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount()) + 1;
            // 用 LambdaUpdateWrapper 只更新需要的字段，避免更新 password
            var wrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUserEntity>()
                    .eq(SysUserEntity::getId, user.getId())
                    .set(SysUserEntity::getLoginErrorCount, errors);
            if (errors >= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 30);
                wrapper.set(SysUserEntity::getLockTime, cal.getTime());
            }
            sysUserDao.update(null, wrapper);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 登录成功，重置错误计数（用 LambdaUpdateWrapper，避免更新 password 字段）
        sysUserDao.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getId, user.getId())
                        .set(SysUserEntity::getLoginErrorCount, 0)
                        .set(SysUserEntity::getLastLoginTime, new Date())
                        .set(SysUserEntity::getLastLoginIp, loginIp));
        // 不返回明文密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        // 登出只需要清除最后登录信息
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
        // 先获取用户的所有角色
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 汇总所有角色的权限码
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
        // 加密密码
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        sysUserDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysUserEntity update(SysUserEntity entity) {
        // 使用 LambdaUpdateWrapper 避免乐观锁冲突问题
        var wrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUserEntity>()
                .eq(SysUserEntity::getId, entity.getId())
                .eq(SysUserEntity::getDeleted, (short) 0);

        // 只更新非空字段
        if (entity.getUsername() != null) wrapper.set(SysUserEntity::getUsername, entity.getUsername());
        if (entity.getRealName() != null) wrapper.set(SysUserEntity::getRealName, entity.getRealName());
        if (entity.getPhone() != null) wrapper.set(SysUserEntity::getPhone, entity.getPhone());
        if (entity.getEmail() != null) wrapper.set(SysUserEntity::getEmail, entity.getEmail());
        if (entity.getDeptId() != null) wrapper.set(SysUserEntity::getDeptId, entity.getDeptId());
        if (entity.getZoneId() != null) wrapper.set(SysUserEntity::getZoneId, entity.getZoneId());
        if (entity.getStatus() != null) wrapper.set(SysUserEntity::getStatus, entity.getStatus());

        // 如果传了新密码则加密
        if (StringUtils.hasText(entity.getPassword())) {
            wrapper.set(SysUserEntity::getPassword, passwordEncoder.encode(entity.getPassword()));
        }

        // 执行更新
        int rows = sysUserDao.update(null, wrapper);
        if (rows == 0) {
            throw new IllegalStateException("用户更新失败，可能已被其他用户修改");
        }
        return sysUserDao.selectById(entity.getId());
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
    public List<SysUserEntity> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<SysUserEntity> list = sysUserDao.selectBatchIds(ids);
        list.forEach(u -> u.setPassword(null)); // 不返回密码
        return list;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("changePassword called: userId={}, oldPwd={}, newPwd={}", userId, oldPassword != null ? "provided" : "null", newPassword != null ? "provided" : "null");
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        // 使用 LambdaUpdateWrapper 更新密码，避免乐观锁冲突
        LambdaUpdateWrapper<SysUserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUserEntity::getId, userId)
               .eq(SysUserEntity::getDeleted, (short) 0)
               .set(SysUserEntity::getPassword, passwordEncoder.encode(newPassword));
        int rows = sysUserDao.update(null, wrapper);
        log.info("changePassword result: rows={}", rows);
        if (rows == 0) {
            throw new IllegalStateException("密码修改失败，用户可能已被删除或禁用");
        }
        return true;
    }

    /** 调试用：重置指定用户的密码（不校验旧密码） */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        LambdaUpdateWrapper<SysUserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUserEntity::getId, userId)
               .set(SysUserEntity::getPassword, passwordEncoder.encode(newPassword));
        sysUserDao.update(null, wrapper);
    }
}
