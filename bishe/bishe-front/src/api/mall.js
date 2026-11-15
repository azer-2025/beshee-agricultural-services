import request from '@/utils/request'

export function listProducts(params) {
  return request({ url: '/agri/buyer/product/list', method: 'get', params })
}

export function getProduct(productId) {
  return request({ url: `/agri/buyer/product/${productId}`, method: 'get' })
}

export function listCategories() {
  return request({ url: '/agri/buyer/category/list', method: 'get' })
}

export function listCart() {
  return request({ url: '/agri/buyer/cart/list', method: 'get' })
}

export function addCart(data) {
  return request({ url: '/agri/buyer/cart/add', method: 'post', data })
}

export function updateCartQty(data) {
  return request({ url: '/agri/buyer/cart/quantity', method: 'put', data })
}

export function updateCartChecked(data) {
  return request({ url: '/agri/buyer/cart/checked', method: 'put', data })
}

export function updateCartCheckedAll(data) {
  return request({ url: '/agri/buyer/cart/checkedAll', method: 'put', data })
}

export function clearInvalidCart() {
  return request({ url: '/agri/buyer/cart/clearInvalid', method: 'delete' })
}

export function removeCart(cartIds) {
  return request({ url: `/agri/buyer/cart/${cartIds}`, method: 'delete' })
}

export function listBuyerCoupons(params) {
  return request({ url: '/agri/buyer/coupon/list', method: 'get', params })
}

export function listActivePromotions() {
  return request({ url: '/agri/buyer/promotion/active', method: 'get' })
}

export function quoteFreight(data) {
  return request({ url: '/agri/buyer/freight/quote', method: 'post', data })
}

export function submitOrder(data) {
  return request({ url: '/agri/buyer/order/submit', method: 'post', data })
}

export function payOrder(data) {
  return request({ url: '/agri/buyer/order/pay', method: 'post', data })
}

export function listOrders(params) {
  return request({ url: '/agri/buyer/order/list', method: 'get', params })
}

export function listOrderItems(orderId) {
  return request({ url: `/agri/buyer/order/items/${orderId}`, method: 'get' })
}

export function listOrderTrace(orderId) {
  return request({ url: `/agri/buyer/order/trace/${orderId}`, method: 'get' })
}

export function getOrderVoucher(orderId) {
  return request({ url: `/agri/buyer/order/voucher/${orderId}`, method: 'get' })
}

export function confirmOrder(orderId) {
  return request({ url: `/agri/buyer/order/confirm/${orderId}`, method: 'put' })
}

export function applyRefund(data) {
  return request({ url: '/agri/buyer/order/refund', method: 'post', data })
}

export function createDispute(data) {
  return request({ url: '/agri/buyer/order/dispute', method: 'post', data })
}

export function listConsults(params) {
  return request({ url: '/agri/buyer/consult/list', method: 'get', params })
}

export function aiChat(data) {
  return request({ url: '/agri/buyer/ai/chat', method: 'post', data })
}

export function listAiHistory(params) {
  return request({ url: '/agri/buyer/ai/history', method: 'get', params })
}

export function clearAiHistory() {
  return request({ url: '/agri/buyer/ai/history', method: 'delete' })
}

export function listPendingReviews() {
  return request({ url: '/agri/buyer/review/pending', method: 'get' })
}

export function listReviews() {
  return request({ url: '/agri/buyer/review/list', method: 'get' })
}

export function addReview(data) {
  return request({ url: '/agri/buyer/review/add', method: 'post', data })
}

export function listAddresses() {
  return request({ url: '/agri/buyer/address/list', method: 'get' })
}

export function saveAddress(data) {
  return request({ url: '/agri/buyer/address/save', method: 'post', data })
}

export function removeAddress(addressId) {
  return request({ url: `/agri/buyer/address/${addressId}`, method: 'delete' })
}

export function setDefaultAddress(addressId) {
  return request({ url: `/agri/buyer/address/default/${addressId}`, method: 'put' })
}
