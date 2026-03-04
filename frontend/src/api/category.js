import request from '../utils/request'

export function listCategories() {
  return request.get('/category/list')
}
