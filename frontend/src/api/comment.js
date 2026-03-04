import request from '../utils/request'

export function listComments(articleId) {
  return request.get(`/comment/list/${articleId}`)
}

export function addComment(data) {
  return request.post('/comment/add', data)
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}
