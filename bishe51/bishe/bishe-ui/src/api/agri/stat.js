import request from '@/utils/request'

// 获取管理员首页统计数据
export function getAdminStat() {
  return request({
    url: '/agri/admin/stat/overview',
    method: 'get'
  })
}

// 获取农户首页统计数据
export function getFarmerStat(params) {
  return request({
    url: '/agri/farmer/stat/overview',
    params: params,
    method: 'get'
  })
}
