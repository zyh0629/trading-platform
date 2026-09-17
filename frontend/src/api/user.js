import request from './request'

export const login = (username, password) => {
  return request.post('/user/login', null, { params: { username, password } })
}

export const register = (data) => {
  return request.post('/user/register', data)
}

export const getUserList = () => {
  return request.get('/user/list')
}