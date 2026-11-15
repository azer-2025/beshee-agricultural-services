import axios from 'axios'

const request = axios.create({
  baseURL: '/dev-api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('front_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.code !== 200) {
      return Promise.reject(new Error(data.msg || 'Request failed'))
    }
    return data
  },
  error => Promise.reject(error)
)

export default request
