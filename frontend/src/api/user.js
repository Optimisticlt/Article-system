import request from '../utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function getProfile(userId) {
  return request.get(`/user/profile/${userId}`)
}

export function updateProfile(data) {
  return request.put('/user/profile', null, { params: data })
}

export function updatePassword(data) {
  return request.put('/user/password', null, { params: data })
}

export function updateAvatar(avatarUrl) {
  return request.put('/user/avatar', null, { params: { avatarUrl } })
}
