import request from '../utils/request'

export function listArticles(params) {
  return request.get('/article/list', { params })
}

export function getArticleDetail(id) {
  return request.get(`/article/detail/${id}`)
}

export function searchArticles(params) {
  return request.get('/article/search', { params })
}

export function listMyArticles(params) {
  return request.get('/article/my', { params })
}

export function publishArticle(data) {
  return request.post('/article/publish', data)
}

export function updateArticle(data) {
  return request.put('/article/update', data)
}

export function deleteArticle(id) {
  return request.delete(`/article/${id}`)
}

export function toggleLike(id) {
  return request.post(`/article/${id}/like`)
}

export function listMyArticlesByUser(params) {
  return request.get('/article/list', { params })
}
