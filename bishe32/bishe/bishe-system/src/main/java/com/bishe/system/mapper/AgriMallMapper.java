package com.bishe.system.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AgriMallMapper
{
    List<Map<String, Object>> selectCategoryList();

    List<Map<String, Object>> selectProductList(Map<String, Object> params);

    Map<String, Object> selectProductDetail(@Param("productId") Long productId);

    Map<String, Object> selectCartItem(@Param("buyerId") Long buyerId, @Param("productId") Long productId);

    int insertCartItem(Map<String, Object> data);

    int updateCartItemQuantity(@Param("cartId") Long cartId, @Param("quantity") Integer quantity, @Param("updateTime") Date updateTime);

    int updateCartItemChecked(@Param("buyerId") Long buyerId, @Param("cartId") Long cartId, @Param("checked") String checked, @Param("updateTime") Date updateTime);

    int updateCartCheckedAll(@Param("buyerId") Long buyerId, @Param("checked") String checked, @Param("updateTime") Date updateTime);

    int deleteCartByIds(@Param("buyerId") Long buyerId, @Param("cartIds") Long[] cartIds);

    int clearInvalidCart(@Param("buyerId") Long buyerId);

    int clearCheckedCart(@Param("buyerId") Long buyerId);

    List<Map<String, Object>> selectCartList(@Param("buyerId") Long buyerId);

    List<Map<String, Object>> selectCheckedCart(@Param("buyerId") Long buyerId);

    int decreaseProductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    int increaseProductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    int increaseProductSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    int insertOrder(Map<String, Object> data);

    int insertOrderItem(Map<String, Object> data);

    List<Map<String, Object>> selectBuyerOrderList(Map<String, Object> params);

    List<Map<String, Object>> selectFarmerOrderList(Map<String, Object> params);

    List<Map<String, Object>> selectAdminOrderList(Map<String, Object> params);

    Map<String, Object> selectOrderByBuyer(@Param("orderId") Long orderId, @Param("buyerId") Long buyerId);

    Map<String, Object> selectOrderById(@Param("orderId") Long orderId);

    Map<String, Object> selectOrderByNo(@Param("orderNo") String orderNo);

    Map<String, Object> selectOrderByFarmer(@Param("orderId") Long orderId, @Param("farmerId") Long farmerId);

    List<Map<String, Object>> selectOrderItemsByOrder(@Param("orderId") Long orderId);

    int updateOrderPay(Map<String, Object> data);

    int updateOrderPayByOrderNo(Map<String, Object> data);

    int updateOrderStatusByBuyer(Map<String, Object> data);

    int updateOrderStatusByFarmer(Map<String, Object> data);

    int updateOrderShip(Map<String, Object> data);

    int updateOrderByAdmin(Map<String, Object> data);

    int updateOrderToRefunding(@Param("orderId") Long orderId, @Param("updateTime") Date updateTime);

    int updateOrderToRefunded(@Param("orderId") Long orderId, @Param("updateTime") Date updateTime);

    int insertPaymentVoucher(Map<String, Object> data);

    Map<String, Object> selectPaymentVoucherByOrder(@Param("orderId") Long orderId);

    int insertLogisticsTrace(Map<String, Object> data);

    List<Map<String, Object>> selectLogisticsByOrder(@Param("orderId") Long orderId);

    Map<String, Object> selectRefundByOrderAndBuyer(@Param("orderId") Long orderId, @Param("buyerId") Long buyerId);

    int insertRefund(Map<String, Object> data);

    List<Map<String, Object>> selectFarmerRefundList(Map<String, Object> params);

    int updateRefundHandle(Map<String, Object> data);

    int insertFarmerNotice(Map<String, Object> data);

    List<Map<String, Object>> selectFarmerNoticeList(Map<String, Object> params);

    int updateFarmerNoticeRead(@Param("noticeId") Long noticeId, @Param("farmerId") Long farmerId, @Param("readTime") Date readTime);

    int updateFarmerNoticeReadAll(@Param("farmerId") Long farmerId, @Param("readTime") Date readTime);

    Integer countFarmerUnreadNotice(@Param("farmerId") Long farmerId);

    List<Long> selectUserIdsByRoleKey(@Param("roleKey") String roleKey);

    int insertAdminNotice(Map<String, Object> data);

    List<Map<String, Object>> selectAdminNoticeList(Map<String, Object> params);

    int updateAdminNoticeRead(@Param("noticeId") Long noticeId, @Param("adminId") Long adminId, @Param("readTime") Date readTime);

    int updateAdminNoticeReadAll(@Param("adminId") Long adminId, @Param("readTime") Date readTime);

    Integer countAdminUnreadNotice(@Param("adminId") Long adminId);

    List<Map<String, Object>> selectReviewableItems(@Param("buyerId") Long buyerId);

    int insertReview(Map<String, Object> data);

    int updateOrderItemReviewStatus(@Param("itemId") Long itemId);

    int updateOrderItemReviewStatusValue(@Param("itemId") Long itemId, @Param("reviewStatus") String reviewStatus);

    Map<String, Object> selectOrderItemById(@Param("itemId") Long itemId);

    Map<String, Object> selectReviewById(@Param("reviewId") Long reviewId);

    int updateReviewByAdmin(Map<String, Object> data);

    int deleteReviewById(@Param("reviewId") Long reviewId);

    Integer countReviewByOrderItem(@Param("itemId") Long itemId);

    List<Map<String, Object>> selectBuyerReviewList(@Param("buyerId") Long buyerId);

    List<Map<String, Object>> selectAdminPendingReviewList(Map<String, Object> params);

    List<Map<String, Object>> selectAdminReviewList(Map<String, Object> params);

    Map<String, Object> selectFarmerApplyByUserId(@Param("userId") Long userId);

    int insertFarmerApply(Map<String, Object> data);

    int updateFarmerApply(Map<String, Object> data);

    List<Map<String, Object>> selectFarmerApplyList(Map<String, Object> params);

    int updateFarmerApplyAudit(Map<String, Object> data);

    List<Map<String, Object>> selectFarmerProductList(Map<String, Object> params);

    int insertProduct(Map<String, Object> data);

    int updateProductByFarmer(Map<String, Object> data);

    int deleteProductByFarmer(@Param("productId") Long productId, @Param("farmerId") Long farmerId);

    List<Map<String, Object>> selectAdminProductList(Map<String, Object> params);

    int updateProductMedia(Map<String, Object> data);

    int updateProductAudit(Map<String, Object> data);

    Map<String, Object> selectFarmerSummary(@Param("farmerId") Long farmerId, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    List<Map<String, Object>> selectFarmerCurve(@Param("farmerId") Long farmerId, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    Map<String, Object> selectAdminSummary();

    List<Map<String, Object>> selectAdminCategoryDistribution();

    List<Map<String, Object>> selectAdminOrderStatusDistribution();

    List<Map<String, Object>> selectAdminSalesCurve(@Param("beginDate") String beginDate, @Param("endDate") String endDate);

    List<Map<String, Object>> selectLatestNotices(@Param("count") Integer count);

    List<Map<String, Object>> selectFarmerHotProducts(@Param("farmerId") Long farmerId);

    Integer countFarmerLowStock(@Param("farmerId") Long farmerId, @Param("threshold") Integer threshold);

    int insertDispute(Map<String, Object> data);

    Map<String, Object> selectDisputeById(@Param("disputeId") Long disputeId);

    List<Map<String, Object>> selectDisputeList(Map<String, Object> params);

    int updateDisputeByAdmin(Map<String, Object> data);

    int updateDisputeHandle(Map<String, Object> data);

    int deleteDisputeById(@Param("disputeId") Long disputeId);

    List<Map<String, Object>> selectFreightTemplateList(Map<String, Object> params);

    int insertFreightTemplate(Map<String, Object> data);

    int updateFreightTemplate(Map<String, Object> data);

    int deleteFreightTemplate(@Param("templateId") Long templateId);

    List<Map<String, Object>> selectBuyerCouponList(@Param("buyerId") Long buyerId);

    Map<String, Object> selectBuyerCouponById(@Param("buyerId") Long buyerId, @Param("userCouponId") Long userCouponId);

    int markUserCouponUsed(Map<String, Object> data);

    int insertUserCoupon(Map<String, Object> data);

    int decreaseCouponRemain(@Param("couponId") Long couponId, @Param("count") Integer count, @Param("operator") String operator,
        @Param("updateTime") Date updateTime);

    List<Map<String, Object>> selectCouponList(Map<String, Object> params);

    int insertCoupon(Map<String, Object> data);

    int updateCoupon(Map<String, Object> data);

    int deleteCoupon(@Param("couponId") Long couponId);

    List<Map<String, Object>> selectPromotionList(Map<String, Object> params);

    List<Map<String, Object>> selectActivePromotionList();

    int insertPromotion(Map<String, Object> data);

    int updatePromotion(Map<String, Object> data);

    int deletePromotion(@Param("promotionId") Long promotionId);

    int deleteOrderItemsByOrder(@Param("orderId") Long orderId);

    int deletePaymentVoucherByOrder(@Param("orderId") Long orderId);

    int deleteLogisticsByOrder(@Param("orderId") Long orderId);

    int deleteRefundByOrder(@Param("orderId") Long orderId);

    int deleteDisputeByOrder(@Param("orderId") Long orderId);

    int deleteOrderById(@Param("orderId") Long orderId);

    List<Map<String, Object>> selectBuyerAddressList(@Param("userId") Long userId);

    int insertBuyerAddress(Map<String, Object> data);

    int updateBuyerAddress(Map<String, Object> data);

    int clearBuyerDefaultAddress(@Param("userId") Long userId, @Param("updateTime") Date updateTime);

    int setBuyerDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId, @Param("updateTime") Date updateTime);

    int deleteBuyerAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);
}
