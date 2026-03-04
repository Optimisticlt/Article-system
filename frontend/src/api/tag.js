import request from '../utils/request'

export function listTags() {
  return request.get('/tag/list')
}
