import request from '@/utils/request'

export function getCaptcha() {
  return request({ url: '/captchaImage', method: 'get' })
}

export function login(data) {
  return request({ url: '/login', method: 'post', data })
}

export function register(data) {
  return request({ url: '/register', method: 'post', data })
}

export function getInfo() {
  return request({ url: '/getInfo', method: 'get' })
}

export function uploadAvatar(data) {
  return request({
    url: '/system/user/profile/avatar',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data
  })
}

export function updateProfile(data) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}
