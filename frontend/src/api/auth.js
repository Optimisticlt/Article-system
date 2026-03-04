import request from '../utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getCaptcha() {
  return request.get('/auth/captcha')
}

export function forgotPassword(email) {
  return request.post('/auth/forgot-password', null, { params: { email } })
}

export function getUserInfo() {
  return request.get('/user/info')
}