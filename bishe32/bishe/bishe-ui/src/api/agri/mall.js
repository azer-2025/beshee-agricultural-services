import request from '@/utils/request'

export function listBuyerCategories() {
  return request({
    url: '/agri/buyer/category/list',
    method: 'get'
  })
}

export function listBuyerProducts(params) {
  return request({
    url: '/agri/buyer/product/list',
    method: 'get',
    params
  })
}

export function getBuyerProduct(productId) {
  return request({
    url: '/agri/buyer/product/' + productId,
    method: 'get'
  })
}

export function listBuyerCart() {
  return request({
    url: '/agri/buyer/cart/list',
    method: 'get'
  })
}

export function addBuyerCart(data) {
  return request({
    url: '/agri/buyer/cart/add',
    method: 'post',
    data
  })
}

export function updateBuyerCartQty(data) {
  return request({
    url: '/agri/buyer/cart/quantity',
    method: 'put',
    data
  })
}

export function updateBuyerCartChecked(data) {
  return request({
    url: '/agri/buyer/cart/checked',
    method: 'put',
    data
  })
}

export function updateBuyerCartCheckedAll(data) {
  return request({
    url: '/agri/buyer/cart/checkedAll',
    method: 'put',
    data
  })
}

export function clearBuyerInvalidCart() {
  return request({
    url: '/agri/buyer/cart/clearInvalid',
    method: 'delete'
  })
}

export function removeBuyerCart(cartIds) {
  return request({
    url: '/agri/buyer/cart/' + cartIds,
    method: 'delete'
  })
}

export function listBuyerCoupons(params) {
  return request({
    url: '/agri/buyer/coupon/list',
    method: 'get',
    params
  })
}

export function submitBuyerOrder(data) {
  return request({
    url: '/agri/buyer/order/submit',
    method: 'post',
    data
  })
}

export function payBuyerOrder(data) {
  return request({
    url: '/agri/buyer/order/pay',
    method: 'post',
    data
  })
}

export function listBuyerOrders(params) {
  return request({
    url: '/agri/buyer/order/list',
    method: 'get',
    params
  })
}

export function listBuyerOrderItems(orderId) {
  return request({
    url: '/agri/buyer/order/items/' + orderId,
    method: 'get'
  })
}

export function listBuyerOrderTrace(orderId) {
  return request({
    url: '/agri/buyer/order/trace/' + orderId,
    method: 'get'
  })
}

export function getBuyerOrderVoucher(orderId) {
  return request({
    url: '/agri/buyer/order/voucher/' + orderId,
    method: 'get'
  })
}

export function confirmBuyerOrder(orderId) {
  return request({
    url: '/agri/buyer/order/confirm/' + orderId,
    method: 'put'
  })
}

export function applyBuyerRefund(data) {
  return request({
    url: '/agri/buyer/order/refund',
    method: 'post',
    data
  })
}

export function createBuyerDispute(data) {
  return request({
    url: '/agri/buyer/order/dispute',
    method: 'post',
    data
  })
}

export function listBuyerPendingReviews() {
  return request({
    url: '/agri/buyer/review/pending',
    method: 'get'
  })
}

export function listBuyerReviews() {
  return request({
    url: '/agri/buyer/review/list',
    method: 'get'
  })
}

export function addBuyerReview(data) {
  return request({
    url: '/agri/buyer/review/add',
    method: 'post',
    data
  })
}

export function getFarmerApply() {
  return request({
    url: '/agri/farmer/apply/my',
    method: 'get'
  })
}

export function submitFarmerApply(data) {
  return request({
    url: '/agri/farmer/apply/submit',
    method: 'post',
    data
  })
}

export function listFarmerProducts(params) {
  return request({
    url: '/agri/farmer/product/list',
    method: 'get',
    params
  })
}

export function saveFarmerProduct(data) {
  return request({
    url: '/agri/farmer/product/save',
    method: 'post',
    data
  })
}

export function removeFarmerProduct(productId) {
  return request({
    url: '/agri/farmer/product/' + productId,
    method: 'delete'
  })
}

export function listFarmerOrders(params) {
  return request({
    url: '/agri/farmer/order/list',
    method: 'get',
    params
  })
}

export function acceptFarmerOrder(orderId, data = {}) {
  return request({
    url: '/agri/farmer/order/accept/' + orderId,
    method: 'put',
    data
  })
}

export function shipFarmerOrder(data) {
  return request({
    url: '/agri/farmer/order/ship',
    method: 'put',
    data
  })
}

export function listFarmerRefunds() {
  return request({
    url: '/agri/farmer/refund/list',
    method: 'get'
  })
}

export function handleFarmerRefund(data) {
  return request({
    url: '/agri/farmer/refund/handle',
    method: 'put',
    data
  })
}

export function listFarmerNotices(params) {
  return request({
    url: '/agri/farmer/notice/list',
    method: 'get',
    params
  })
}

export function countFarmerUnreadNotices() {
  return request({
    url: '/agri/farmer/notice/unreadCount',
    method: 'get'
  })
}

export function readFarmerNotice(noticeId) {
  return request({
    url: '/agri/farmer/notice/read/' + noticeId,
    method: 'put'
  })
}

export function readAllFarmerNotice() {
  return request({
    url: '/agri/farmer/notice/readAll',
    method: 'put'
  })
}

export function listAdminNotices(params) {
  return request({
    url: '/agri/admin/notice/list',
    method: 'get',
    params
  })
}

export function countAdminUnreadNotices() {
  return request({
    url: '/agri/admin/notice/unreadCount',
    method: 'get'
  })
}

export function readAdminNotice(noticeId) {
  return request({
    url: '/agri/admin/notice/read/' + noticeId,
    method: 'put'
  })
}

export function readAllAdminNotice() {
  return request({
    url: '/agri/admin/notice/readAll',
    method: 'put'
  })
}

export function getFarmerStats(params) {
  return request({
    url: '/agri/farmer/stat/overview',
    method: 'get',
    params
  })
}

export function listAdminFarmerApplies(params) {
  return request({
    url: '/agri/admin/farmer/list',
    method: 'get',
    params
  })
}

export function auditAdminFarmerApply(data) {
  return request({
    url: '/agri/admin/farmer/audit',
    method: 'put',
    data
  })
}

export function listAdminProducts(params) {
  return request({
    url: '/agri/admin/product/list',
    method: 'get',
    params
  })
}

export function auditAdminProduct(data) {
  return request({
    url: '/agri/admin/product/audit',
    method: 'put',
    data
  })
}

export function updateAdminProductMedia(data) {
  return request({
    url: '/agri/admin/product/media',
    method: 'put',
    data
  })
}

export function saveAdminProduct(data) {
  return request({
    url: '/agri/admin/product/save',
    method: 'post',
    data
  })
}

export function removeAdminProduct(productId) {
  return request({
    url: '/agri/admin/product/' + productId,
    method: 'delete'
  })
}

export function listAdminOrders(params) {
  return request({
    url: '/agri/admin/order/list',
    method: 'get',
    params
  })
}

export function listAdminOrderItems(orderId) {
  return request({
    url: '/agri/admin/order/items/' + orderId,
    method: 'get'
  })
}

export function listAdminOrderTrace(orderId) {
  return request({
    url: '/agri/admin/order/trace/' + orderId,
    method: 'get'
  })
}

export function getAdminOrderVoucher(orderId) {
  return request({
    url: '/agri/admin/order/voucher/' + orderId,
    method: 'get'
  })
}

export function createAdminOrder(data) {
  return request({
    url: '/agri/admin/order/create',
    method: 'post',
    data
  })
}

export function updateAdminOrder(data) {
  return request({
    url: '/agri/admin/order/update',
    method: 'put',
    data
  })
}

export function removeAdminOrder(orderId) {
  return request({
    url: '/agri/admin/order/' + orderId,
    method: 'delete'
  })
}

export function listAdminDisputes(params) {
  return request({
    url: '/agri/admin/dispute/list',
    method: 'get',
    params
  })
}

export function handleAdminDispute(data) {
  return request({
    url: '/agri/admin/dispute/handle',
    method: 'put',
    data
  })
}

export function listAdminConsults(params) {
  return request({
    url: '/agri/admin/consult/list',
    method: 'get',
    params
  })
}

export function saveAdminConsult(data) {
  return request({
    url: '/agri/admin/consult/save',
    method: 'post',
    data
  })
}

export function removeAdminConsult(disputeId) {
  return request({
    url: '/agri/admin/consult/' + disputeId,
    method: 'delete'
  })
}

export function listAdminPendingReviews(params) {
  return request({
    url: '/agri/admin/review/pending/list',
    method: 'get',
    params
  })
}

export function listAdminReviews(params) {
  return request({
    url: '/agri/admin/review/list',
    method: 'get',
    params
  })
}

export function saveAdminReview(data) {
  return request({
    url: '/agri/admin/review/save',
    method: 'post',
    data
  })
}

export function removeAdminReview(reviewId) {
  return request({
    url: '/agri/admin/review/' + reviewId,
    method: 'delete'
  })
}

export function listFreightTemplates(params) {
  return request({
    url: '/agri/admin/freight/list',
    method: 'get',
    params
  })
}

export function saveFreightTemplate(data) {
  return request({
    url: '/agri/admin/freight/save',
    method: 'post',
    data
  })
}

export function removeFreightTemplate(templateId) {
  return request({
    url: '/agri/admin/freight/' + templateId,
    method: 'delete'
  })
}

export function listCoupons(params) {
  return request({
    url: '/agri/admin/coupon/list',
    method: 'get',
    params
  })
}

export function saveCoupon(data) {
  return request({
    url: '/agri/admin/coupon/save',
    method: 'post',
    data
  })
}

export function removeCoupon(couponId) {
  return request({
    url: '/agri/admin/coupon/' + couponId,
    method: 'delete'
  })
}

export function issueCoupon(data) {
  return request({
    url: '/agri/admin/coupon/issue',
    method: 'post',
    data
  })
}

export function listPromotions(params) {
  return request({
    url: '/agri/admin/promotion/list',
    method: 'get',
    params
  })
}

export function savePromotion(data) {
  return request({
    url: '/agri/admin/promotion/save',
    method: 'post',
    data
  })
}

export function removePromotion(promotionId) {
  return request({
    url: '/agri/admin/promotion/' + promotionId,
    method: 'delete'
  })
}
