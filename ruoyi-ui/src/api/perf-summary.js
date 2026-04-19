import request from '@/utils/request'

/***********************************************************************
 * 业绩汇总 API
 * 对应后端：GET /api/perfSummary/summary
 ***********************************************************************/

/**
 * 获取业绩汇总数据
 * @param {Object} params - 查询参数
 * @param {string} [params.beginTime]   - 统计开始日期，格式 yyyy-MM-dd
 * @param {string} [params.endTime]     - 统计结束日期，格式 yyyy-MM-dd
 * @param {number} [params.deptId]      - 部门ID（可选）
 * @param {number} [params.zoneId]      - 战区ID（可选）
 * @param {string} [params.groupBy='sales_rep'] - 聚合维度：
 *        sales_rep=按销售人员, dept=按部门, zone=按战区, month=按月份
 * @returns {Promise} 汇总结果
 */
export function getSummary(params) {
  return request({
    url: '/perfSummary/summary',
    method: 'get',
    params
  })
}
