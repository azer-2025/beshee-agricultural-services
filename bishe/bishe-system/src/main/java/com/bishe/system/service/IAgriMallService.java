package com.bishe.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IAgriMallService
{
    List<Map<String, Object>> selectCategoryList();

    List<Map<String, Object>> selectProductList(Map<String, Object> query);

    Map<String, Object> selectProductDetail(Long productId);

    List<Map<String, Object>> selectCartList(Long buyerId);

    void addCart(Long buyerId, Long productId, Integer quantity);

    void updateCartQuantity(Long buyerId, Long cartId, Integer quantity);

    void updateCartChecked(Long buyerId, Long cartId, String checked);

    void updateCartCheckedAll(Long buyerId, String checked);

    void removeCart(Long buyerId, Long[] cartIds);

    int clearInvalidCart(Long buyerId);

    Map<String, Object> quoteBuyerFreight(Long buyerId, Map<String, Object> form);

    Map<String, Object> submitOrder(Long buyerId, Map<String, Object> form);

    Map<String, Object> mockPay(Long buyerId, Long orderId, String channel);

    List<Map<String, Object>> selectBuyerOrderList(Long buyerId, Map<String, Object> query);

    List<Map<String, Object>> selectFarmerOrderList(Long farmerId, Map<String, Object> query);

    List<Map<String, Object>> selectAdminOrderList(Map<String, Object> query);

    void acceptOrder(Long farmerId, Long orderId, String remark);

    void shipOrder(Long farmerId, Long orderId, String remark);

    List<Map<String, Object>> selectOrderItems(Long buyerId, Long orderId);

    List<Map<String, Object>> selectOrderTrace(Long buyerId, Long orderId);

    Map<String, Object> selectOrderVoucher(Long buyerId, Long orderId);

    List<Map<String, Object>> selectAdminOrderItems(Long orderId);

    List<Map<String, Object>> selectAdminOrderTrace(Long orderId);

    Map<String, Object> selectAdminOrderVoucher(Long orderId);

    Map<String, Object> createAdminOrder(String operator, Map<String, Object> form);

    void updateAdminOrder(String operator, Map<String, Object> form);

    void removeAdminOrder(String operator, Long orderId);

    void confirmReceipt(Long buyerId, Long orderId);

    void applyRefund(Long buyerId, Long orderId, String reason);

    List<Map<String, Object>> selectFarmerRefundList(Long farmerId);

    void handleRefund(Long farmerId, Long refundId, String status, String remark);

    List<Map<String, Object>> selectFarmerNoticeList(Long farmerId, Map<String, Object> query);

    int countFarmerUnreadNotice(Long farmerId);

    void readFarmerNotice(Long farmerId, Long noticeId);

    int readAllFarmerNotice(Long farmerId);

    List<Map<String, Object>> selectReviewableItems(Long buyerId);

    List<Map<String, Object>> selectBuyerReviewList(Long buyerId);

    void addReview(Long buyerId, Map<String, Object> form);

    List<Map<String, Object>> selectAdminPendingReviewList(Map<String, Object> query);

    List<Map<String, Object>> selectAdminReviewList(Map<String, Object> query);

    void saveAdminReview(String operator, Map<String, Object> form);

    void removeAdminReview(Long reviewId);

    Map<String, Object> selectFarmerApplyByUserId(Long userId);

    void submitFarmerApply(Long userId, Map<String, Object> form);

    List<Map<String, Object>> selectFarmerApplyList(Map<String, Object> query);

    void auditFarmerApply(String auditBy, Long applyId, String status, String auditRemark);

    List<Map<String, Object>> selectFarmerProductList(Long farmerId, Map<String, Object> query);

    void saveFarmerProduct(Long farmerId, Map<String, Object> form);

    void removeFarmerProduct(Long farmerId, Long productId);

    List<Map<String, Object>> selectAdminProductList(Map<String, Object> query);

    void saveAdminProduct(String operator, Map<String, Object> form);

    void removeAdminProduct(Long productId);

    void updateAdminProductMedia(String operator, Long productId, String productCover, String productImages);

    void auditProduct(String auditBy, Long productId, String status, String auditRemark);

    Map<String, Object> selectFarmerStats(Long farmerId, String period);

    Map<String, Object> selectAdminStats();

    void createDispute(Long buyerId, Long orderId, String title, String content);

    List<Map<String, Object>> selectBuyerDisputeList(Long buyerId, Map<String, Object> query);

    List<Map<String, Object>> selectDisputeList(Map<String, Object> query);

    void saveAdminDispute(String operator, Map<String, Object> form);

    void removeAdminDispute(Long disputeId);

    void handleDispute(String handleBy, Long disputeId, String status, String handleResult);

    List<Map<String, Object>> selectAdminNoticeList(Long adminId, Map<String, Object> query);

    int countAdminUnreadNotice(Long adminId);

    void readAdminNotice(Long adminId, Long noticeId);

    int readAllAdminNotice(Long adminId);

    List<Map<String, Object>> selectFreightTemplateList(Map<String, Object> query);

    void saveFreightTemplate(String operator, Map<String, Object> form);

    void removeFreightTemplate(Long templateId);

    List<Map<String, Object>> selectBuyerCouponList(Long buyerId, BigDecimal orderAmount);

    List<Map<String, Object>> selectBuyerActivePromotionList();

    List<Map<String, Object>> selectCouponList(Map<String, Object> query);

    void saveCoupon(String operator, Map<String, Object> form);

    void removeCoupon(Long couponId);

    void issueCoupon(String operator, Long couponId, Long[] userIds);

    List<Map<String, Object>> selectPromotionList(Map<String, Object> query);

    void savePromotion(String operator, Map<String, Object> form);

    void removePromotion(Long promotionId);

    List<Map<String, Object>> selectBuyerAddressList(Long userId);

    void saveBuyerAddress(Long userId, Map<String, Object> form);

    void removeBuyerAddress(Long userId, Long addressId);

    void setDefaultAddress(Long userId, Long addressId);
}
