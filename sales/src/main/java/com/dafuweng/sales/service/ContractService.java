package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.vo.ContractDetailVO;
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

import java.util.List;

public interface ContractService {

    ContractEntity getById(Long id);

    ContractEntity getByContractNo(String contractNo);

    PageResponse<ContractEntity> pageList(PageRequest request, String filterRole, Long userId, Long deptId, Long zoneId, String contractNo);

    List<ContractEntity> listBySalesRepId(Long salesRepId);

    List<ContractEntity> listByStatus(Short status);

    /**
     * 按状态分页查询合同
     */
    PageResponse<ContractEntity> pageListByStatus(int pageNum, int pageSize, Short status);

    /**
     * 按状态分页查询合同（含关联名称）
     */
    PageResponse<ContractDetailVO> pageListByStatusWithNames(int pageNum, int pageSize, Short status);

    @Transactional
    ContractEntity save(ContractEntity entity);

    @Transactional
    ContractEntity update(ContractEntity entity);

    @Transactional
    void delete(Long id);

    Long getMinUnusedId();

    /**
     * 生成新合同编号
     * 格式：HT-YYYYMMDD-XXXX
     */
    String generateContractNo();

    /**
     * 获取合同详情（含关联信息）
     */
    ContractEntity getDetail(Long id);

    /**
     * 获取合同详情（含关联名称）
     */
    ContractDetailVO getDetailWithNames(Long id);

    /**
     * 获取合同总数
     */
    Long count();

    /**
     * 按状态获取合同数量
     */
    Long countByStatus(Short status);

    /**
     * 标记合同已支付首期
     * status=2 -> status=3
     */
    void payFirstInstallment(Long id);

    /**
     * 提交合同至金融部
     * status=3 -> status=4
     */
    void submitToFinance(Long id);

    @Transactional
    void bankLoan(Long id);
}