package com.bishe.web.controller.agri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.system.service.IAgriMallService;

@RestController
@RequestMapping("/agri")
public class AgriMallController extends BaseController
{
    @Autowired
    private IAgriMallService agriMallService;

    @GetMapping("/buyer/category/list")
    public AjaxResult buyerCategoryList()
    {
        return success(agriMallService.selectCategoryList());
    }

    @GetMapping("/buyer/product/list")
    public TableDataInfo buyerProductList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectProductList(query));
    }

    @GetMapping("/buyer/product/{productId}")
    public AjaxResult buyerProductDetail(@PathVariable Long productId)
    {
        return success(agriMallService.selectProductDetail(productId));
    }

    @GetMapping("/buyer/cart/list")
    public AjaxResult buyerCartList()
    {
        return success(agriMallService.selectCartList(getUserId()));
    }

    @PostMapping("/buyer/cart/add")
    public AjaxResult addCart(@RequestBody Map<String, Object> form)
    {
        agriMallService.addCart(getUserId(), toLong(form.get("productId")), toInt(form.get("quantity")));
        return success();
    }

    @PutMapping("/buyer/cart/quantity")
    public AjaxResult updateCartQty(@RequestBody Map<String, Object> form)
    {
        agriMallService.updateCartQuantity(getUserId(), toLong(form.get("cartId")), toInt(form.get("quantity")));
        return success();
    }

    @PutMapping("/buyer/cart/checked")
    public AjaxResult updateCartChecked(@RequestBody Map<String, Object> form)
    {
        agriMallService.updateCartChecked(getUserId(), toLong(form.get("cartId")), String.valueOf(form.get("checked")));
        return success();
    }

    @PutMapping("/buyer/cart/checkedAll")
    public AjaxResult updateCartCheckedAll(@RequestBody Map<String, Object> form)
    {
        agriMallService.updateCartCheckedAll(getUserId(), String.valueOf(form.get("checked")));
        return success();
    }

    @DeleteMapping("/buyer/cart/{cartIds}")
    public AjaxResult removeCart(@PathVariable Long[] cartIds)
    {
        agriMallService.removeCart(getUserId(), cartIds);
        return success();
    }

    @DeleteMapping("/buyer/cart/clearInvalid")
    public AjaxResult clearInvalidCart()
    {
        return success(agriMallService.clearInvalidCart(getUserId()));
    }

    @GetMapping("/buyer/coupon/list")
    public AjaxResult buyerCouponList(@RequestParam(required = false) BigDecimal orderAmount)
    {
        return success(agriMallService.selectBuyerCouponList(getUserId(), orderAmount));
    }

    @GetMapping("/buyer/promotion/active")
    public AjaxResult buyerActivePromotionList()
    {
        return success(agriMallService.selectBuyerActivePromotionList());
    }

    @PostMapping("/buyer/freight/quote")
    public AjaxResult buyerFreightQuote(@RequestBody(required = false) Map<String, Object> form)
    {
        return success(agriMallService.quoteBuyerFreight(getUserId(), form));
    }

    @PostMapping("/buyer/order/submit")
    public AjaxResult submitOrder(@RequestBody Map<String, Object> form)
    {
        return success(agriMallService.submitOrder(getUserId(), form));
    }

    @PostMapping("/buyer/order/pay")
    public AjaxResult mockPay(@RequestBody Map<String, Object> form)
    {
        String channel = form.get("channel") == null ? null : String.valueOf(form.get("channel"));
        return success(agriMallService.mockPay(getUserId(), toLong(form.get("orderId")), channel));
    }

    @GetMapping("/buyer/order/list")
    public TableDataInfo buyerOrderList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectBuyerOrderList(getUserId(), query));
    }

    @GetMapping("/buyer/order/items/{orderId}")
    public AjaxResult buyerOrderItems(@PathVariable Long orderId)
    {
        return success(agriMallService.selectOrderItems(getUserId(), orderId));
    }

    @GetMapping("/buyer/order/trace/{orderId}")
    public AjaxResult buyerOrderTrace(@PathVariable Long orderId)
    {
        return success(agriMallService.selectOrderTrace(getUserId(), orderId));
    }

    @GetMapping("/buyer/order/voucher/{orderId}")
    public AjaxResult buyerOrderVoucher(@PathVariable Long orderId)
    {
        return success(agriMallService.selectOrderVoucher(getUserId(), orderId));
    }

    @PutMapping("/buyer/order/confirm/{orderId}")
    public AjaxResult confirmReceipt(@PathVariable Long orderId)
    {
        agriMallService.confirmReceipt(getUserId(), orderId);
        return success();
    }

    @PostMapping("/buyer/order/refund")
    public AjaxResult applyRefund(@RequestBody Map<String, Object> form)
    {
        agriMallService.applyRefund(getUserId(), toLong(form.get("orderId")), String.valueOf(form.get("reason")));
        return success();
    }

    @PostMapping("/buyer/order/dispute")
    public AjaxResult createDispute(@RequestBody Map<String, Object> form)
    {
        agriMallService.createDispute(getUserId(), toLong(form.get("orderId")), String.valueOf(form.get("title")), String.valueOf(form.get("content")));
        return success();
    }

    @GetMapping("/buyer/consult/list")
    public TableDataInfo buyerConsultList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectBuyerDisputeList(getUserId(), query));
    }

    @GetMapping("/buyer/review/pending")
    public AjaxResult pendingReviewList()
    {
        return success(agriMallService.selectReviewableItems(getUserId()));
    }

    @GetMapping("/buyer/review/list")
    public AjaxResult buyerReviewList()
    {
        return success(agriMallService.selectBuyerReviewList(getUserId()));
    }

    @PostMapping("/buyer/review/add")
    public AjaxResult addReview(@RequestBody Map<String, Object> form)
    {
        agriMallService.addReview(getUserId(), form);
        return success();
    }

    @GetMapping("/buyer/address/list")
    public AjaxResult buyerAddressList()
    {
        return success(agriMallService.selectBuyerAddressList(getUserId()));
    }

    @PostMapping("/buyer/address/save")
    public AjaxResult saveBuyerAddress(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveBuyerAddress(getUserId(), form);
        return success();
    }

    @DeleteMapping("/buyer/address/{addressId}")
    public AjaxResult removeBuyerAddress(@PathVariable Long addressId)
    {
        agriMallService.removeBuyerAddress(getUserId(), addressId);
        return success();
    }

    @PutMapping("/buyer/address/default/{addressId}")
    public AjaxResult setDefaultAddress(@PathVariable Long addressId)
    {
        agriMallService.setDefaultAddress(getUserId(), addressId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:apply:list')")
    @GetMapping("/farmer/apply/my")
    public AjaxResult myApply()
    {
        return success(agriMallService.selectFarmerApplyByUserId(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:apply:list')")
    @PostMapping("/farmer/apply/submit")
    public AjaxResult submitApply(@RequestBody Map<String, Object> form)
    {
        agriMallService.submitFarmerApply(getUserId(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:product:list')")
    @GetMapping("/farmer/product/list")
    public TableDataInfo farmerProductList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectFarmerProductList(getUserId(), query));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:product:list')")
    @PostMapping("/farmer/product/save")
    public AjaxResult saveFarmerProduct(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveFarmerProduct(getUserId(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:product:list')")
    @DeleteMapping("/farmer/product/{productId}")
    public AjaxResult removeFarmerProduct(@PathVariable Long productId)
    {
        agriMallService.removeFarmerProduct(getUserId(), productId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:order:list')")
    @GetMapping("/farmer/order/list")
    public TableDataInfo farmerOrderList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectFarmerOrderList(getUserId(), query));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:order:accept')")
    @PutMapping("/farmer/order/accept/{orderId}")
    public AjaxResult acceptOrder(@PathVariable Long orderId, @RequestBody(required = false) Map<String, Object> form)
    {
        String remark = form == null ? "" : String.valueOf(form.get("remark"));
        agriMallService.acceptOrder(getUserId(), orderId, remark);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:order:ship')")
    @PutMapping("/farmer/order/ship")
    public AjaxResult shipOrder(@RequestBody Map<String, Object> form)
    {
        agriMallService.shipOrder(getUserId(), toLong(form.get("orderId")), String.valueOf(form.get("remark")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:refund:list')")
    @GetMapping("/farmer/refund/list")
    public AjaxResult farmerRefundList()
    {
        return success(agriMallService.selectFarmerRefundList(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:refund:handle')")
    @PutMapping("/farmer/refund/handle")
    public AjaxResult handleRefund(@RequestBody Map<String, Object> form)
    {
        agriMallService.handleRefund(getUserId(), toLong(form.get("refundId")), String.valueOf(form.get("status")), String.valueOf(form.get("remark")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:notice:list')")
    @GetMapping("/farmer/notice/list")
    public TableDataInfo farmerNoticeList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectFarmerNoticeList(getUserId(), query));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:notice:list')")
    @GetMapping("/farmer/notice/unreadCount")
    public AjaxResult farmerNoticeUnreadCount()
    {
        return success(agriMallService.countFarmerUnreadNotice(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:notice:read')")
    @PutMapping("/farmer/notice/read/{noticeId}")
    public AjaxResult readFarmerNotice(@PathVariable Long noticeId)
    {
        agriMallService.readFarmerNotice(getUserId(), noticeId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:notice:read')")
    @PutMapping("/farmer/notice/readAll")
    public AjaxResult readAllFarmerNotice()
    {
        return success(agriMallService.readAllFarmerNotice(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:farmer:stat:list')")
    @GetMapping("/farmer/stat/overview")
    public AjaxResult farmerStat(@RequestParam(required = false) String period)
    {
        return success(agriMallService.selectFarmerStats(getUserId(), period));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:farmer:list')")
    @GetMapping("/admin/farmer/list")
    public TableDataInfo adminFarmerApplyList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectFarmerApplyList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:farmer:list')")
    @PutMapping("/admin/farmer/audit")
    public AjaxResult adminAuditFarmer(@RequestBody Map<String, Object> form)
    {
        agriMallService.auditFarmerApply(getUsername(), toLong(form.get("applyId")), String.valueOf(form.get("status")), String.valueOf(form.get("auditRemark")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:product:list')")
    @GetMapping("/admin/product/list")
    public TableDataInfo adminProductList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectAdminProductList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:product:list')")
    @PostMapping("/admin/product/save")
    public AjaxResult adminSaveProduct(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveAdminProduct(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:product:list')")
    @DeleteMapping("/admin/product/{productId}")
    public AjaxResult adminRemoveProduct(@PathVariable Long productId)
    {
        agriMallService.removeAdminProduct(productId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:product:list')")
    @PutMapping("/admin/product/media")
    public AjaxResult adminUpdateProductMedia(@RequestBody Map<String, Object> form)
    {
        agriMallService.updateAdminProductMedia(
            getUsername(),
            toLong(form.get("productId")),
            form.get("productCover") == null ? null : String.valueOf(form.get("productCover")),
            form.get("productImages") == null ? null : String.valueOf(form.get("productImages"))
        );
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:product:list')")
    @PutMapping("/admin/product/audit")
    public AjaxResult adminAuditProduct(@RequestBody Map<String, Object> form)
    {
        agriMallService.auditProduct(getUsername(), toLong(form.get("productId")), String.valueOf(form.get("status")), String.valueOf(form.get("auditRemark")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @GetMapping("/admin/order/list")
    public TableDataInfo adminOrderList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectAdminOrderList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @GetMapping("/admin/order/items/{orderId}")
    public AjaxResult adminOrderItems(@PathVariable Long orderId)
    {
        return success(agriMallService.selectAdminOrderItems(orderId));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @GetMapping("/admin/order/trace/{orderId}")
    public AjaxResult adminOrderTrace(@PathVariable Long orderId)
    {
        return success(agriMallService.selectAdminOrderTrace(orderId));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @GetMapping("/admin/order/voucher/{orderId}")
    public AjaxResult adminOrderVoucher(@PathVariable Long orderId)
    {
        return success(agriMallService.selectAdminOrderVoucher(orderId));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @PostMapping("/admin/order/create")
    public AjaxResult adminCreateOrder(@RequestBody Map<String, Object> form)
    {
        return success(agriMallService.createAdminOrder(getUsername(), form));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @PutMapping("/admin/order/update")
    public AjaxResult adminUpdateOrder(@RequestBody Map<String, Object> form)
    {
        agriMallService.updateAdminOrder(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:order:list')")
    @DeleteMapping("/admin/order/{orderId}")
    public AjaxResult adminRemoveOrder(@PathVariable Long orderId)
    {
        agriMallService.removeAdminOrder(getUsername(), orderId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:dispute:list')")
    @GetMapping("/admin/dispute/list")
    public TableDataInfo adminDisputeList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectDisputeList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:dispute:list')")
    @PutMapping("/admin/dispute/handle")
    public AjaxResult adminHandleDispute(@RequestBody Map<String, Object> form)
    {
        agriMallService.handleDispute(getUsername(), toLong(form.get("disputeId")), String.valueOf(form.get("status")), String.valueOf(form.get("handleResult")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:list')")
    @GetMapping("/admin/consult/list")
    public TableDataInfo adminConsultList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectDisputeList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:list')")
    @PostMapping("/admin/consult/save")
    public AjaxResult adminSaveConsult(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveAdminDispute(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:list')")
    @DeleteMapping("/admin/consult/{disputeId}")
    public AjaxResult adminRemoveConsult(@PathVariable Long disputeId)
    {
        agriMallService.removeAdminDispute(disputeId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:review:list')")
    @GetMapping("/admin/review/pending/list")
    public TableDataInfo adminPendingReviewList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectAdminPendingReviewList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:review:list')")
    @GetMapping("/admin/review/list")
    public TableDataInfo adminReviewList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectAdminReviewList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:review:save')")
    @PostMapping("/admin/review/save")
    public AjaxResult adminSaveReview(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveAdminReview(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:review:remove')")
    @DeleteMapping("/admin/review/{reviewId}")
    public AjaxResult adminRemoveReview(@PathVariable Long reviewId)
    {
        agriMallService.removeAdminReview(reviewId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:list')")
    @GetMapping("/admin/notice/list")
    public TableDataInfo adminNoticeList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectAdminNoticeList(getUserId(), query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:list')")
    @GetMapping("/admin/notice/unreadCount")
    public AjaxResult adminNoticeUnreadCount()
    {
        return success(agriMallService.countAdminUnreadNotice(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:read')")
    @PutMapping("/admin/notice/read/{noticeId}")
    public AjaxResult readAdminNotice(@PathVariable Long noticeId)
    {
        agriMallService.readAdminNotice(getUserId(), noticeId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:notice:read')")
    @PutMapping("/admin/notice/readAll")
    public AjaxResult readAllAdminNotice()
    {
        return success(agriMallService.readAllAdminNotice(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @GetMapping("/admin/freight/list")
    public TableDataInfo freightList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectFreightTemplateList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @PostMapping("/admin/freight/save")
    public AjaxResult saveFreight(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveFreightTemplate(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @DeleteMapping("/admin/freight/{templateId}")
    public AjaxResult removeFreight(@PathVariable Long templateId)
    {
        agriMallService.removeFreightTemplate(templateId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @GetMapping("/admin/coupon/list")
    public TableDataInfo couponList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectCouponList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @PostMapping("/admin/coupon/save")
    public AjaxResult saveCoupon(@RequestBody Map<String, Object> form)
    {
        agriMallService.saveCoupon(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @DeleteMapping("/admin/coupon/{couponId}")
    public AjaxResult removeCoupon(@PathVariable Long couponId)
    {
        agriMallService.removeCoupon(couponId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @PostMapping("/admin/coupon/issue")
    public AjaxResult issueCoupon(@RequestBody Map<String, Object> form)
    {
        agriMallService.issueCoupon(getUsername(), toLong(form.get("couponId")), toLongArray(form.get("userIds")));
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @GetMapping("/admin/promotion/list")
    public TableDataInfo promotionList(@RequestParam Map<String, Object> query)
    {
        startPage();
        return getDataTable(agriMallService.selectPromotionList(query));
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @PostMapping("/admin/promotion/save")
    public AjaxResult savePromotion(@RequestBody Map<String, Object> form)
    {
        agriMallService.savePromotion(getUsername(), form);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:operate:list')")
    @DeleteMapping("/admin/promotion/{promotionId}")
    public AjaxResult removePromotion(@PathVariable Long promotionId)
    {
        agriMallService.removePromotion(promotionId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('agri:admin:stat:list')")
    @GetMapping("/admin/stat/overview")
    public AjaxResult adminStat()
    {
        return success(agriMallService.selectAdminStats());
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer toInt(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private Long[] toLongArray(Object value)
    {
        if (value == null)
        {
            return new Long[0];
        }
        List<Long> result = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                Long id = toLong(item);
                if (id != null)
                {
                    result.add(id);
                }
            }
        }
        else
        {
            String[] parts = String.valueOf(value).split(",");
            for (String part : parts)
            {
                String text = part == null ? "" : part.trim();
                if (text.isEmpty())
                {
                    continue;
                }
                Long id = toLong(text);
                if (id != null)
                {
                    result.add(id);
                }
            }
        }
        return result.toArray(new Long[0]);
    }
}
