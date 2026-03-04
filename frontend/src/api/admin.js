import request from '../utils/request'

// Dashboard
export function getDashboard() {
  return request.get('/admin/dashboard')
}

// Article management
export function adminListArticles(params) {
  return request.get('/admin/article/list', { params })
}

export function adminUpdateArticleStatus(id, status) {
  return request.put(`/admin/article/status/${id}`, null, { params: { status } })
}

export function adminToggleTop(id) {
  return request.put(`/admin/article/top/${id}`)
}

export function adminDeleteArticle(id) {
  return request.delete(`/admin/article/${id}`)
}

// Category management
export function adminListCategories() {
  return request.get('/admin/category/list')
}

export function adminAddCategory(data) {
  return request.post('/admin/category', null, { params: data })
}

export function adminUpdateCategory(id, data) {
  return request.put(`/admin/category/${id}`, null, { params: data })
}

export function adminDeleteCategory(id) {
  return request.delete(`/admin/category/${id}`)
}

// Tag management
export function adminListTags() {
  return request.get('/admin/tag/list')
}

export function adminAddTag(name) {
  return request.post('/admin/tag', null, { params: { name } })
}

export function adminDeleteTag(id) {
  return request.delete(`/admin/tag/${id}`)
}

// User management
export function adminListUsers(params) {
  return request.get('/admin/user/list', { params })
}

export function adminUpdateUserStatus(id, status) {
  return request.put(`/admin/user/status/${id}`, null, { params: { status } })
}

// Comment management
export function adminListComments(params) {
  return request.get('/admin/comment/list', { params })
}

export function adminDeleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}
