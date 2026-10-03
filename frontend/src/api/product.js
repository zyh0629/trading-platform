import request from './request'

// 注意：request 拦截器已经提取了 data，所以返回的就是数据本身
export const getProductList = (spotId) => request.get('/product/list', {
  params: spotId ? { spotId } : {}
})
export const getRecommendList = (userId) => request.get(`/recommend/list/${userId}`)
export const recordView = (userId, productId) => request.post(`/recommend/view?userId=${userId}&productId=${productId}`)
export const getProductsByCategory = (categoryId, spotId) => request.get(`/product/category/${categoryId}`, {
  params: spotId ? { spotId } : {}
})