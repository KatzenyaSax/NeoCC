import request from '@/utils/request'

// 获取业绩排名列表
export function getRankingList(params) {
  return request({
    url: '/perfRanking/list',
    method: 'get',
    params
  })
}

// 导出排名（可选）
export function exportRanking(params) {
  return request({
    url: '/perfRanking/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
