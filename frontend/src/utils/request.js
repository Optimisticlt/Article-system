import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
const service = axios.create({ baseURL: '/api', timeout: 10000 })
service.interceptors.request.use(config => { const token = localStorage.getItem('accessToken'); if (token) config.headers.Authorization = 'Bearer ' + token; return config }, error => Promise.reject(error))
service.interceptors.response.use(response => {
  const res = response.data
  if (res.code !== 200) {
    ElMessage.error(res.message || 'Request failed')
    if (res.code === 401) { const rt = localStorage.getItem('refreshToken'); if (rt) return tryRefreshToken(rt, response.config); localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); router.push('/login') }
    return Promise.reject(new Error(res.message))
  }
  return res
}, error => { ElMessage.error(error.message || 'Network error'); return Promise.reject(error) })
let isRefreshing = false; let pendingRequests = []
async function tryRefreshToken(refreshToken, originalConfig) {
  if (isRefreshing) return new Promise(resolve => { pendingRequests.push(config => { config.headers.Authorization = 'Bearer ' + localStorage.getItem('accessToken'); resolve(service(config)) }) }).then(() => service(originalConfig))
  isRefreshing = true
  try { const res = await axios.post('/api/auth/refresh-token', null, { params: { refreshToken } }); if (res.data.code === 200) { localStorage.setItem('accessToken', res.data.data.accessToken); localStorage.setItem('refreshToken', res.data.data.refreshToken); if (res.data.data.role) localStorage.setItem('userRole', res.data.data.role); pendingRequests.forEach(cb => cb(originalConfig)); pendingRequests = []; originalConfig.headers.Authorization = 'Bearer ' + res.data.data.accessToken; return service(originalConfig) } } catch {} finally { isRefreshing = false }
  localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); router.push('/login')
  return Promise.reject(new Error('Session expired'))
}
export default service
