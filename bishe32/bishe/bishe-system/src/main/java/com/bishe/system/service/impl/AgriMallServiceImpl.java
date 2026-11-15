package com.bishe.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.mapper.AgriMallMapper;
import com.bishe.system.service.IAgriMallService;

@Service
public class AgriMallServiceImpl implements IAgriMallService
{
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ADMIN_ROLE_KEY = "agri_admin";
    private static final String SYS_ADMIN_ROLE_KEY = "admin";
    private static final String REGION_NORTH = "CN-NORTH";
    private static final String REGION_EAST = "CN-EAST";
    private static final String REGION_CENTRAL = "CN-CENTRAL";
    private static final String REGION_SOUTH = "CN-SOUTH";
    private static final String REGION_NORTHEAST = "CN-NORTHEAST";
    private static final String REGION_SOUTHWEST = "CN-SOUTHWEST";
    private static final String REGION_NORTHWEST = "CN-NORTHWEST";
    private static final String REGION_HK_MO_TW = "CN-HK-MO-TW";
    private static final Pattern PROMOTION_FOLD_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*折");
    private static final Pattern PROMOTION_PERCENT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%");
    private static final Pattern PROMOTION_FULL_REDUCE_PATTERN = Pattern.compile("满\\s*(\\d+(?:\\.\\d+)?)\\s*减\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern PROMOTION_DIRECT_REDUCE_PATTERN = Pattern.compile("(?:立减|减)\\s*(\\d+(?:\\.\\d+)?)");

    @Autowired
    private AgriMallMapper agriMallMapper;

    @Autowired
    private AgriMallPayService agriMallPayService;

    @Override
    public List<Map<String, Object>> selectCategoryList()
    {
        return agriMallMapper.selectCategoryList();
    }

    @Override
    public List<Map<String, Object>> selectProductList(Map<String, Object> query)
    {
        List<Map<String, Object>> products = agriMallMapper.selectProductList(defaultMap(query));
        applyPromotionPriceToList(products, loadActivePromotions());
        return products;
    }

    @Override
    public Map<String, Object> selectProductDetail(Long productId)
    {
        Map<String, Object> detail = agriMallMapper.selectProductDetail(productId);
        if (detail == null)
        {
            throw new ServiceException("Product not found");
        }
        applyPromotionPriceToItem(detail, loadActivePromotions());
        return detail;
    }

    @Override
    public List<Map<String, Object>> selectCartList(Long buyerId)
    {
        List<Map<String, Object>> cartList = agriMallMapper.selectCartList(buyerId);
        applyPromotionPriceToList(cartList, loadActivePromotions());
        return cartList;
    }

    @Override
    public void addCart(Long buyerId, Long productId, Integer quantity)
    {
        if (productId == null)
        {
            throw new ServiceException("Product is required");
        }
        int qty = quantity == null || quantity <= 0 ? 1 : quantity;
        Map<String, Object> product = agriMallMapper.selectProductDetail(productId);
        if (product == null || !"1".equals(toStr(product.get("status"))))
        {
            throw new ServiceException("Product is not available");
        }
        int stock = toInt(product.get("stock"), 0);
        if (stock <= 0)
        {
            throw new ServiceException("Product out of stock");
        }

        Date now = new Date();
        Map<String, Object> existing = agriMallMapper.selectCartItem(buyerId, productId);
        if (existing == null)
        {
            Map<String, Object> insertMap = new HashMap<>();
            insertMap.put("buyerId", buyerId);
            insertMap.put("productId", productId);
            insertMap.put("quantity", Math.min(qty, stock));
            insertMap.put("checked", "1");
            insertMap.put("createTime", now);
            insertMap.put("updateTime", now);
            agriMallMapper.insertCartItem(insertMap);
            return;
        }

        Long cartId = toLong(existing.get("cartId"));
        int currentQty = toInt(existing.get("quantity"), 0);
        int newQty = Math.min(currentQty + qty, stock);
        agriMallMapper.updateCartItemQuantity(cartId, newQty, now);
        agriMallMapper.updateCartItemChecked(buyerId, cartId, "1", now);
    }

    @Override
    public void updateCartQuantity(Long buyerId, Long cartId, Integer quantity)
    {
        if (cartId == null || quantity == null || quantity <= 0)
        {
            throw new ServiceException("Invalid cart params");
        }
        List<Map<String, Object>> carts = agriMallMapper.selectCartList(buyerId);
        Map<String, Object> target = carts.stream().filter(item -> Objects.equals(toLong(item.get("cartId")), cartId)).findFirst().orElse(null);
        if (target == null)
        {
            throw new ServiceException("Cart item not found");
        }
        if (toInt(target.get("invalidFlag"), 0) == 1)
        {
            throw new ServiceException("Cart item is invalid");
        }
        int stock = toInt(target.get("stock"), 0);
        if (quantity > stock)
        {
            throw new ServiceException("Quantity exceeds stock");
        }
        agriMallMapper.updateCartItemQuantity(cartId, quantity, new Date());
    }

    @Override
    public void updateCartChecked(Long buyerId, Long cartId, String checked)
    {
        agriMallMapper.updateCartItemChecked(buyerId, cartId, "1".equals(checked) ? "1" : "0", new Date());
    }

    @Override
    public void updateCartCheckedAll(Long buyerId, String checked)
    {
        agriMallMapper.updateCartCheckedAll(buyerId, "1".equals(checked) ? "1" : "0", new Date());
    }

    @Override
    public void removeCart(Long buyerId, Long[] cartIds)
    {
        if (cartIds == null || cartIds.length == 0)
        {
            throw new ServiceException("No cart items selected");
        }
        agriMallMapper.deleteCartByIds(buyerId, cartIds);
    }

    @Override
    public int clearInvalidCart(Long buyerId)
    {
        return agriMallMapper.clearInvalidCart(buyerId);
    }

    @Override
    public Map<String, Object> quoteBuyerFreight(Long buyerId, Map<String, Object> form)
    {
        Map<String, Object> request = defaultMap(form);
        String receiverAddress = trimToEmpty(request.get("receiverAddress"));
        if (StringUtils.isEmpty(receiverAddress))
        {
            throw new ServiceException("Receiver address is required");
        }
        List<Map<String, Object>> checkedItems = agriMallMapper.selectCheckedCart(buyerId);
        Map<Long, List<Map<String, Object>>> grouped = checkedItems == null
            ? new LinkedHashMap<>()
            : checkedItems.stream().collect(Collectors.groupingBy(item -> toLong(item.get("farmerId")), LinkedHashMap::new, Collectors.toList()));
        String regionCode = resolveFreightRegionCode(request, receiverAddress);
        Map<String, Object> quote = calculateFreightResult(regionCode, grouped);
        quote.remove("orderFreightMap");
        return quote;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitOrder(Long buyerId, Map<String, Object> form)
    {
        List<Map<String, Object>> checkedItems = agriMallMapper.selectCheckedCart(buyerId);
        if (checkedItems == null || checkedItems.isEmpty())
        {
            throw new ServiceException("No available checked cart items");
        }
        applyPromotionPriceToList(checkedItems, loadActivePromotions());

        String receiverName = trimToEmpty(form.get("receiverName"));
        String receiverPhone = trimToEmpty(form.get("receiverPhone"));
        String receiverAddress = trimToEmpty(form.get("receiverAddress"));
        String buyerRemark = trimToEmpty(form.get("buyerRemark"));
        String paymentChannel = StringUtils.isEmpty(toStr(form.get("paymentChannel"))) ? "2" : toStr(form.get("paymentChannel"));
        Long userCouponId = toLong(form.get("userCouponId"));
        if (StringUtils.isEmpty(receiverName) || StringUtils.isEmpty(receiverPhone) || StringUtils.isEmpty(receiverAddress))
        {
            throw new ServiceException("Receiver info is required");
        }

        Map<Long, List<Map<String, Object>>> grouped = checkedItems.stream()
            .collect(Collectors.groupingBy(item -> toLong(item.get("farmerId")), LinkedHashMap::new, Collectors.toList()));
        String regionCode = resolveFreightRegionCode(defaultMap(form), receiverAddress);
        Map<String, Object> freightResult = calculateFreightResult(regionCode, grouped);
        @SuppressWarnings("unchecked")
        Map<Long, BigDecimal> orderFreightMap = (Map<Long, BigDecimal>) freightResult.get("orderFreightMap");

        Date now = new Date();
        BigDecimal originTotalPay = BigDecimal.ZERO;
        Map<Long, BigDecimal> orderOriginPayMap = new LinkedHashMap<>();

        for (Map.Entry<Long, List<Map<String, Object>>> entry : grouped.entrySet())
        {
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (Map<String, Object> item : entry.getValue())
            {
                orderTotal = orderTotal.add(toBigDecimal(item.get("price")).multiply(BigDecimal.valueOf(toInt(item.get("quantity"), 0))));
            }
            BigDecimal orderPay = orderTotal.add(orderFreightMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            orderOriginPayMap.put(entry.getKey(), orderPay);
            originTotalPay = originTotalPay.add(orderPay);
        }

        BigDecimal couponDiscount = BigDecimal.ZERO;
        Map<String, Object> selectedCoupon = null;
        if (userCouponId != null)
        {
            selectedCoupon = agriMallMapper.selectBuyerCouponById(buyerId, userCouponId);
            if (selectedCoupon == null)
            {
                throw new ServiceException("Coupon not found or unavailable");
            }
            couponDiscount = calculateCouponDiscount(selectedCoupon, originTotalPay);
            if (couponDiscount.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("Current order does not meet coupon requirements");
            }
        }

        Map<Long, BigDecimal> discountShareMap = splitDiscount(originTotalPay, couponDiscount, orderOriginPayMap);
        List<Map<String, Object>> createdOrders = new ArrayList<>();
        BigDecimal totalPay = BigDecimal.ZERO;

        for (Map.Entry<Long, List<Map<String, Object>>> entry : grouped.entrySet())
        {
            Long farmerId = entry.getKey();
            List<Map<String, Object>> farmerItems = entry.getValue();
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (Map<String, Object> item : farmerItems)
            {
                orderTotal = orderTotal.add(toBigDecimal(item.get("price")).multiply(BigDecimal.valueOf(toInt(item.get("quantity"), 0))));
            }
            BigDecimal orderFreight = orderFreightMap.getOrDefault(farmerId, BigDecimal.ZERO);
            BigDecimal originOrderPay = orderTotal.add(orderFreight);
            BigDecimal orderDiscount = discountShareMap.getOrDefault(farmerId, BigDecimal.ZERO);
            BigDecimal payAmount = originOrderPay.subtract(orderDiscount);
            if (payAmount.compareTo(BigDecimal.ZERO) < 0)
            {
                payAmount = BigDecimal.ZERO;
            }

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderNo", generateOrderNo());
            orderMap.put("buyerId", buyerId);
            orderMap.put("farmerId", farmerId);
            orderMap.put("totalAmount", orderTotal);
            orderMap.put("freightAmount", orderFreight);
            orderMap.put("payAmount", payAmount);
            orderMap.put("payStatus", "0");
            orderMap.put("orderStatus", "0");
            orderMap.put("paymentChannel", paymentChannel);
            orderMap.put("payVoucherNo", "");
            orderMap.put("receiverName", receiverName);
            orderMap.put("receiverPhone", receiverPhone);
            orderMap.put("receiverAddress", receiverAddress);
            orderMap.put("buyerRemark", buyerRemark);
            orderMap.put("farmerRemark", "");
            orderMap.put("createTime", now);
            orderMap.put("updateTime", now);
            agriMallMapper.insertOrder(orderMap);

            Long orderId = toLong(orderMap.get("orderId"));
            for (Map<String, Object> item : farmerItems)
            {
                Long productId = toLong(item.get("productId"));
                int qty = toInt(item.get("quantity"), 0);
                int affected = agriMallMapper.decreaseProductStock(productId, qty);
                if (affected == 0)
                {
                    throw new ServiceException("Insufficient stock for product: " + productId);
                }
                agriMallMapper.increaseProductSales(productId, qty);

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("orderId", orderId);
                itemMap.put("productId", productId);
                itemMap.put("productName", trimToEmpty(item.get("productName")));
                itemMap.put("productCover", trimToEmpty(item.get("productCover")));
                itemMap.put("price", toBigDecimal(item.get("price")));
                itemMap.put("quantity", qty);
                itemMap.put("amount", toBigDecimal(item.get("price")).multiply(BigDecimal.valueOf(qty)));
                itemMap.put("reviewStatus", "0");
                agriMallMapper.insertOrderItem(itemMap);
            }

            Map<String, Object> traceMap = new HashMap<>();
            traceMap.put("orderId", orderId);
            traceMap.put("statusNode", "ORDER_CREATED");
            traceMap.put("statusDesc", "Order created and waiting for payment");
            traceMap.put("traceTime", now);
            agriMallMapper.insertLogisticsTrace(traceMap);

            Map<String, Object> created = new HashMap<>();
            created.put("orderId", orderId);
            created.put("orderNo", orderMap.get("orderNo"));
            created.put("payAmount", payAmount);
            created.put("couponDiscount", orderDiscount);
            created.put("farmerId", farmerId);
            created.put("freightAmount", orderFreight);
            created.put("freightTemplateName", freightResult.get("templateName"));
            created.put("freightRegionCode", freightResult.get("regionCode"));
            addFarmerNotice(
                farmerId,
                "NEW_ORDER",
                orderId,
                "New order pending",
                "Order " + orderMap.get("orderNo") + " amount " + payAmount.toPlainString() + ", please process in time",
                now);
            createdOrders.add(created);
            totalPay = totalPay.add(payAmount);
        }

        if (userCouponId != null)
        {
            Map<String, Object> useMap = new HashMap<>();
            useMap.put("userCouponId", userCouponId);
            useMap.put("buyerId", buyerId);
            useMap.put("usedTime", now);
            useMap.put("usedOrderId", createdOrders.isEmpty() ? null : createdOrders.get(0).get("orderId"));
            useMap.put("discountAmount", couponDiscount);
            useMap.put("updateTime", now);
            int rows = agriMallMapper.markUserCouponUsed(useMap);
            if (rows == 0)
            {
                throw new ServiceException("Coupon use failed");
            }
        }

        agriMallMapper.clearCheckedCart(buyerId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderCount", createdOrders.size());
        result.put("totalPay", totalPay);
        result.put("couponDiscount", couponDiscount);
        result.put("originTotalPay", originTotalPay);
        result.put("freightAmount", freightResult.get("freightAmount"));
        result.put("freightTemplateName", freightResult.get("templateName"));
        result.put("freightRegionCode", freightResult.get("regionCode"));
        result.put("orders", createdOrders);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockPay(Long buyerId, Long orderId, String channel)
    {
        return agriMallPayService.payOrder(buyerId, orderId, channel);
    }

    @Override
    public List<Map<String, Object>> selectBuyerOrderList(Long buyerId, Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        params.put("buyerId", buyerId);
        List<Map<String, Object>> rows = agriMallMapper.selectBuyerOrderList(params);
        boolean changed = agriMallPayService.syncPendingAlipayOrders(rows);
        if (changed)
        {
            rows = agriMallMapper.selectBuyerOrderList(params);
        }
        return enrichOrderFreightMeta(rows);
    }

    @Override
    public List<Map<String, Object>> selectFarmerOrderList(Long farmerId, Map<String, Object> query)
    {
        boolean isAdmin = isMallAdminUser(farmerId);
        Map<String, Object> params = defaultMap(query);
        params.put("farmerId", farmerId);
        params.put("isAdmin", isAdmin ? "1" : "0");
        List<Map<String, Object>> rows = agriMallMapper.selectFarmerOrderList(params);
        boolean changed = agriMallPayService.syncPendingAlipayOrders(rows);
        if (changed)
        {
            rows = agriMallMapper.selectFarmerOrderList(params);
        }
        return enrichOrderFreightMeta(rows);
    }

    @Override
    public List<Map<String, Object>> selectAdminOrderList(Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        List<Map<String, Object>> rows = agriMallMapper.selectAdminOrderList(params);
        boolean changed = agriMallPayService.syncPendingAlipayOrders(rows);
        if (changed)
        {
            rows = agriMallMapper.selectAdminOrderList(params);
        }
        return enrichOrderFreightMeta(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long farmerId, Long orderId, String remark)
    {
        boolean isAdmin = isMallAdminUser(farmerId);
        Map<String, Object> order = isAdmin ? agriMallMapper.selectOrderById(orderId) : agriMallMapper.selectOrderByFarmer(orderId, farmerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        if (!"1".equals(toStr(order.get("pay_status"))))
        {
            throw new ServiceException("Only paid orders can be accepted");
        }
        if (!"0".equals(toStr(order.get("order_status"))))
        {
            throw new ServiceException("Order cannot be accepted in current status");
        }
        Long targetFarmerId = toLong(order.get("farmer_id"));
        if (targetFarmerId == null)
        {
            throw new ServiceException("Order farmer data is missing");
        }
        Date now = new Date();
        Map<String, Object> update = new HashMap<>();
        update.put("orderId", orderId);
        update.put("farmerId", targetFarmerId);
        update.put("orderStatus", "1");
        update.put("farmerRemark", trimToEmpty(remark));
        update.put("updateTime", now);
        agriMallMapper.updateOrderStatusByFarmer(update);
        addTrace(orderId, "ACCEPTED", "Farmer accepted order", now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long farmerId, Long orderId, String remark)
    {
        boolean isAdmin = isMallAdminUser(farmerId);
        Map<String, Object> order = isAdmin ? agriMallMapper.selectOrderById(orderId) : agriMallMapper.selectOrderByFarmer(orderId, farmerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        if (!"1".equals(toStr(order.get("order_status"))))
        {
            throw new ServiceException("Only pending shipment orders can be shipped");
        }
        Long targetFarmerId = toLong(order.get("farmer_id"));
        if (targetFarmerId == null)
        {
            throw new ServiceException("Order farmer data is missing");
        }
        Date now = new Date();
        Map<String, Object> update = new HashMap<>();
        update.put("orderId", orderId);
        update.put("farmerId", targetFarmerId);
        update.put("orderStatus", "2");
        update.put("shipTime", now);
        update.put("farmerRemark", trimToEmpty(remark));
        update.put("updateTime", now);
        agriMallMapper.updateOrderShip(update);
        addTrace(orderId, "SHIPPED", "Farmer shipped order", now);
    }

    @Override
    public List<Map<String, Object>> selectOrderItems(Long buyerId, Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return agriMallMapper.selectOrderItemsByOrder(orderId);
    }

    @Override
    public List<Map<String, Object>> selectOrderTrace(Long buyerId, Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return buildOrderTrace(orderId, order);
    }

    @Override
    public Map<String, Object> selectOrderVoucher(Long buyerId, Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return buildOrderVoucher(orderId, order);
    }

    @Override
    public List<Map<String, Object>> selectAdminOrderItems(Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return agriMallMapper.selectOrderItemsByOrder(orderId);
    }

    @Override
    public List<Map<String, Object>> selectAdminOrderTrace(Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return buildOrderTrace(orderId, order);
    }

    @Override
    public Map<String, Object> selectAdminOrderVoucher(Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        return buildOrderVoucher(orderId, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAdminOrder(String operator, Map<String, Object> form)
    {
        Map<String, Object> request = defaultMap(form);
        Long buyerId = toLong(request.get("buyerId"));
        Long farmerId = toLong(request.get("farmerId"));
        if (buyerId == null || farmerId == null)
        {
            throw new ServiceException("Buyer ID and farmer ID are required");
        }

        BigDecimal payAmount = toBigDecimal(request.get("payAmount"));
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("Pay amount must be greater than 0");
        }
        BigDecimal freightAmount = toBigDecimal(request.get("freightAmount"));
        if (freightAmount.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("Freight amount cannot be less than 0");
        }
        BigDecimal totalAmount = toBigDecimal(request.get("totalAmount"));
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            totalAmount = payAmount;
        }

        String payStatus = trimToEmpty(request.get("payStatus"));
        String orderStatus = trimToEmpty(request.get("orderStatus"));
        String paymentChannel = trimToEmpty(request.get("paymentChannel"));
        if (StringUtils.isEmpty(payStatus))
        {
            payStatus = "0";
        }
        if (StringUtils.isEmpty(orderStatus))
        {
            orderStatus = "0";
        }
        if (StringUtils.isEmpty(paymentChannel))
        {
            paymentChannel = "1";
        }
        if (!isValidPayStatus(payStatus))
        {
            throw new ServiceException("Invalid pay status");
        }
        if (!isValidOrderStatus(orderStatus))
        {
            throw new ServiceException("Invalid order status");
        }

        Date now = new Date();
        String orderNo = generateOrderNo();
        Date payTime = ("1".equals(payStatus) || "2".equals(payStatus)) ? now : null;
        Date shipTime = ("2".equals(orderStatus) || "3".equals(orderStatus) || "4".equals(orderStatus)) ? now : null;
        Date finishTime = "4".equals(orderStatus) ? now : null;

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderNo", orderNo);
        orderMap.put("buyerId", buyerId);
        orderMap.put("farmerId", farmerId);
        orderMap.put("totalAmount", totalAmount);
        orderMap.put("freightAmount", freightAmount);
        orderMap.put("payAmount", payAmount);
        orderMap.put("payStatus", payStatus);
        orderMap.put("orderStatus", orderStatus);
        orderMap.put("paymentChannel", paymentChannel);
        orderMap.put("payVoucherNo", ("1".equals(payStatus) || "2".equals(payStatus)) ? generatePayNo() : "");
        orderMap.put("receiverName", trimToEmpty(request.get("receiverName")));
        orderMap.put("receiverPhone", trimToEmpty(request.get("receiverPhone")));
        orderMap.put("receiverAddress", trimToEmpty(request.get("receiverAddress")));
        orderMap.put("buyerRemark", trimToEmpty(request.get("buyerRemark")));
        orderMap.put("farmerRemark", trimToEmpty(request.get("farmerRemark")));
        orderMap.put("payTime", payTime);
        orderMap.put("shipTime", shipTime);
        orderMap.put("finishTime", finishTime);
        orderMap.put("createTime", now);
        orderMap.put("updateTime", now);
        agriMallMapper.insertOrder(orderMap);

        Long orderId = toLong(orderMap.get("orderId"));
        if (orderId == null)
        {
            throw new ServiceException("Order create failed");
        }

        addTrace(orderId, "ORDER_CREATED", "Admin created order", now);
        if ("1".equals(payStatus) || "2".equals(payStatus))
        {
            addTrace(orderId, "PAID", "Admin marked as paid", now);
        }
        if ("1".equals(orderStatus) || "2".equals(orderStatus) || "3".equals(orderStatus) || "4".equals(orderStatus))
        {
            addTrace(orderId, "ACCEPTED", "Admin set order accepted", now);
        }
        if ("2".equals(orderStatus) || "3".equals(orderStatus) || "4".equals(orderStatus))
        {
            addTrace(orderId, "SHIPPED", "Admin set order shipped", now);
        }
        if ("3".equals(orderStatus) || "4".equals(orderStatus))
        {
            addTrace(orderId, "OUT_FOR_DELIVERY", "Admin set order delivery", now);
        }
        if ("4".equals(orderStatus))
        {
            addTrace(orderId, "RECEIVED", "Admin set order received", now);
        }

        addFarmerNotice(
            farmerId,
            "ORDER_CREATE",
            orderId,
            "有新的订单需要处理",
            "订单 " + orderNo + " 已创建，请及时处理",
            now
        );

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", orderNo);
        result.put("operator", trimToEmpty(operator));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminOrder(String operator, Map<String, Object> form)
    {
        Map<String, Object> request = defaultMap(form);
        Long orderId = toLong(request.get("orderId"));
        if (orderId == null)
        {
            throw new ServiceException("Order ID is required");
        }
        Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }

        String currentPayStatus = toStr(order.get("pay_status"));
        String currentOrderStatus = toStr(order.get("order_status"));
        String payStatus = trimToEmpty(request.get("payStatus"));
        String orderStatus = trimToEmpty(request.get("orderStatus"));
        String paymentChannel = trimToEmpty(request.get("paymentChannel"));
        String farmerRemark = request.containsKey("farmerRemark") ? trimToEmpty(request.get("farmerRemark")) : null;

        if (StringUtils.isEmpty(payStatus) && StringUtils.isEmpty(orderStatus) && StringUtils.isEmpty(paymentChannel) && farmerRemark == null)
        {
            throw new ServiceException("No fields to update");
        }
        if (!StringUtils.isEmpty(payStatus) && !isValidPayStatus(payStatus))
        {
            throw new ServiceException("Invalid pay status");
        }
        if (!StringUtils.isEmpty(orderStatus) && !isValidOrderStatus(orderStatus))
        {
            throw new ServiceException("Invalid order status");
        }

        Date now = new Date();
        Map<String, Object> update = new HashMap<>();
        update.put("orderId", orderId);
        update.put("updateTime", now);
        if (!StringUtils.isEmpty(payStatus))
        {
            update.put("payStatus", payStatus);
        }
        if (!StringUtils.isEmpty(orderStatus))
        {
            update.put("orderStatus", orderStatus);
            if ("2".equals(orderStatus) && !"2".equals(currentOrderStatus))
            {
                update.put("shipTime", now);
            }
            if ("4".equals(orderStatus) && !"4".equals(currentOrderStatus))
            {
                update.put("finishTime", now);
            }
        }
        if (!StringUtils.isEmpty(paymentChannel))
        {
            update.put("paymentChannel", paymentChannel);
        }
        if (farmerRemark != null)
        {
            update.put("farmerRemark", farmerRemark);
        }

        int rows = agriMallMapper.updateOrderByAdmin(update);
        if (rows <= 0)
        {
            throw new ServiceException("Order update failed");
        }

        if (!StringUtils.isEmpty(payStatus) && !payStatus.equals(currentPayStatus) && "1".equals(payStatus))
        {
            addTrace(orderId, "PAID", "Admin updated payment status", now);
        }
        if (!StringUtils.isEmpty(orderStatus) && !orderStatus.equals(currentOrderStatus))
        {
            addTrace(orderId, traceNodeByStatus(orderStatus), "Admin updated order status", now);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAdminOrder(String operator, Long orderId)
    {
        if (orderId == null)
        {
            throw new ServiceException("Order ID is required");
        }
        Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        String orderStatus = toStr(order.get("order_status"));
        if (!("4".equals(orderStatus) || "5".equals(orderStatus) || "7".equals(orderStatus)))
        {
            throw new ServiceException("Only completed, cancelled or refunded orders can be deleted");
        }
        agriMallMapper.deleteOrderItemsByOrder(orderId);
        agriMallMapper.deletePaymentVoucherByOrder(orderId);
        agriMallMapper.deleteLogisticsByOrder(orderId);
        agriMallMapper.deleteRefundByOrder(orderId);
        agriMallMapper.deleteDisputeByOrder(orderId);
        agriMallMapper.deleteOrderById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long buyerId, Long orderId)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        String orderStatus = toStr(order.get("order_status"));
        if (!("2".equals(orderStatus) || "3".equals(orderStatus)))
        {
            throw new ServiceException("Current order status does not support receipt");
        }

        Date now = new Date();
        Map<String, Object> update = new HashMap<>();
        update.put("orderId", orderId);
        update.put("buyerId", buyerId);
        update.put("orderStatus", "4");
        update.put("finishTime", now);
        update.put("updateTime", now);
        agriMallMapper.updateOrderStatusByBuyer(update);

        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put("orderId", orderId);
        traceMap.put("statusNode", "RECEIVED");
        traceMap.put("statusDesc", "Buyer confirmed receipt");
        traceMap.put("traceTime", now);
        agriMallMapper.insertLogisticsTrace(traceMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long buyerId, Long orderId, String reason)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }
        if (!"1".equals(toStr(order.get("pay_status"))))
        {
            throw new ServiceException("Only paid orders can apply refund");
        }
        String orderStatus = toStr(order.get("order_status"));
        if (!("1".equals(orderStatus) || "2".equals(orderStatus) || "3".equals(orderStatus)))
        {
            throw new ServiceException("Current order status cannot apply refund");
        }
        if (agriMallMapper.selectRefundByOrderAndBuyer(orderId, buyerId) != null)
        {
            throw new ServiceException("Refund request already exists");
        }

        Date now = new Date();
        Map<String, Object> refund = new HashMap<>();
        refund.put("orderId", orderId);
        refund.put("buyerId", buyerId);
        refund.put("farmerId", toLong(order.get("farmer_id")));
        refund.put("amount", toBigDecimal(order.get("pay_amount")));
        refund.put("reason", StringUtils.isEmpty(reason) ? "No reason provided" : reason);
        refund.put("status", "0");
        refund.put("farmerRemark", "");
        refund.put("adminRemark", "");
        refund.put("createTime", now);
        refund.put("auditTime", null);
        refund.put("updateTime", now);
        agriMallMapper.insertRefund(refund);
        agriMallMapper.updateOrderToRefunding(orderId, now);
        Long refundId = toLong(refund.get("refundId"));
        addFarmerNotice(
            toLong(order.get("farmer_id")),
            "REFUND_APPLY",
            refundId == null ? orderId : refundId,
            "Refund request pending",
            "Order " + toStr(order.get("order_no")) + " has a new refund request",
            now);

        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put("orderId", orderId);
        traceMap.put("statusNode", "REFUND_APPLY");
        traceMap.put("statusDesc", "Buyer submitted refund request");
        traceMap.put("traceTime", now);
        agriMallMapper.insertLogisticsTrace(traceMap);
    }

    @Override
    public List<Map<String, Object>> selectFarmerRefundList(Long farmerId)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("farmerId", farmerId);
        params.put("isAdmin", isMallAdminUser(farmerId) ? "1" : "0");
        return agriMallMapper.selectFarmerRefundList(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(Long farmerId, Long refundId, String status, String remark)
    {
        boolean isAdmin = isMallAdminUser(farmerId);
        Map<String, Object> query = new HashMap<>();
        query.put("farmerId", farmerId);
        query.put("isAdmin", isAdmin ? "1" : "0");
        List<Map<String, Object>> refunds = agriMallMapper.selectFarmerRefundList(query);
        Map<String, Object> target = refunds.stream().filter(row -> Objects.equals(toLong(row.get("refundId")), refundId)).findFirst().orElse(null);
        if (target == null)
        {
            throw new ServiceException("Refund request not found");
        }
        if (!"0".equals(toStr(target.get("status"))))
        {
            throw new ServiceException("Refund request has been handled");
        }
        if (!("1".equals(status) || "2".equals(status)))
        {
            throw new ServiceException("Invalid refund status");
        }

        Date now = new Date();
        Long targetFarmerId = toLong(target.get("farmerId"));
        if (targetFarmerId == null)
        {
            throw new ServiceException("Refund farmer data is missing");
        }
        Map<String, Object> update = new HashMap<>();
        update.put("refundId", refundId);
        update.put("farmerId", targetFarmerId);
        update.put("status", status);
        update.put("farmerRemark", trimToEmpty(remark));
        update.put("auditTime", now);
        update.put("updateTime", now);
        agriMallMapper.updateRefundHandle(update);

        Long orderId = toLong(target.get("orderId"));
        if ("1".equals(status))
        {
            agriMallMapper.updateOrderToRefunded(orderId, now);
            List<Map<String, Object>> items = agriMallMapper.selectOrderItemsByOrder(orderId);
            for (Map<String, Object> item : items)
            {
                agriMallMapper.increaseProductStock(toLong(item.get("productId")), toInt(item.get("quantity"), 0));
            }
            addTrace(orderId, "REFUND_APPROVED", "Farmer approved refund", now);
        }
        else
        {
            Map<String, Object> orderUpdate = new HashMap<>();
            orderUpdate.put("orderId", orderId);
            orderUpdate.put("farmerId", targetFarmerId);
            orderUpdate.put("orderStatus", "1");
            orderUpdate.put("farmerRemark", trimToEmpty(remark));
            orderUpdate.put("updateTime", now);
            agriMallMapper.updateOrderStatusByFarmer(orderUpdate);
            addTrace(orderId, "REFUND_REJECTED", "Farmer rejected refund", now);
        }
    }

    @Override
    public List<Map<String, Object>> selectFarmerNoticeList(Long farmerId, Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        params.put("farmerId", farmerId);
        return agriMallMapper.selectFarmerNoticeList(params);
    }

    @Override
    public int countFarmerUnreadNotice(Long farmerId)
    {
        Integer count = agriMallMapper.countFarmerUnreadNotice(farmerId);
        return count == null ? 0 : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readFarmerNotice(Long farmerId, Long noticeId)
    {
        if (noticeId == null)
        {
            throw new ServiceException("Notice id is required");
        }
        int rows = agriMallMapper.updateFarmerNoticeRead(noticeId, farmerId, new Date());
        if (rows == 0)
        {
            throw new ServiceException("Notice not found");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int readAllFarmerNotice(Long farmerId)
    {
        return agriMallMapper.updateFarmerNoticeReadAll(farmerId, new Date());
    }

    @Override
    public List<Map<String, Object>> selectReviewableItems(Long buyerId)
    {
        return agriMallMapper.selectReviewableItems(buyerId);
    }

    @Override
    public List<Map<String, Object>> selectBuyerReviewList(Long buyerId)
    {
        return agriMallMapper.selectBuyerReviewList(buyerId);
    }

    @Override
    public List<Map<String, Object>> selectAdminPendingReviewList(Map<String, Object> query)
    {
        return agriMallMapper.selectAdminPendingReviewList(defaultMap(query));
    }

    @Override
    public List<Map<String, Object>> selectAdminReviewList(Map<String, Object> query)
    {
        return agriMallMapper.selectAdminReviewList(defaultMap(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminReview(String operator, Map<String, Object> form)
    {
        Long reviewId = toLong(form.get("reviewId"));
        if (reviewId == null)
        {
            Long itemId = toLong(form.get("itemId"));
            if (itemId == null)
            {
                throw new ServiceException("Order item is required");
            }
            Map<String, Object> orderItem = agriMallMapper.selectOrderItemById(itemId);
            if (orderItem == null)
            {
                throw new ServiceException("Order item not found");
            }
            if ("1".equals(toStr(orderItem.get("reviewStatus"))))
            {
                throw new ServiceException("Order item has already been reviewed");
            }
            Long orderId = toLong(orderItem.get("orderId"));
            Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
            if (order == null)
            {
                throw new ServiceException("Order not found");
            }
            if (!"4".equals(toStr(order.get("order_status"))))
            {
                throw new ServiceException("Only completed orders can be reviewed");
            }

            Date now = new Date();
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("orderId", orderId);
            reviewMap.put("orderItemId", itemId);
            reviewMap.put("productId", toLong(orderItem.get("productId")));
            reviewMap.put("buyerId", toLong(order.get("buyer_id")));
            reviewMap.put("farmerId", toLong(order.get("farmer_id")));
            reviewMap.put("qualityStar", normalizeStar(form.get("qualityStar")));
            reviewMap.put("packageStar", normalizeStar(form.get("packageStar")));
            reviewMap.put("serviceStar", normalizeStar(form.get("serviceStar")));
            reviewMap.put("content", trimToEmpty(form.get("content")));
            reviewMap.put("createTime", now);
            agriMallMapper.insertReview(reviewMap);
            agriMallMapper.updateOrderItemReviewStatusValue(itemId, "1");
            return;
        }

        Map<String, Object> existing = agriMallMapper.selectReviewById(reviewId);
        if (existing == null)
        {
            throw new ServiceException("Review not found");
        }
        Map<String, Object> update = new HashMap<>();
        update.put("reviewId", reviewId);
        update.put("qualityStar", normalizeStar(form.get("qualityStar")));
        update.put("packageStar", normalizeStar(form.get("packageStar")));
        update.put("serviceStar", normalizeStar(form.get("serviceStar")));
        update.put("content", trimToEmpty(form.get("content")));
        int rows = agriMallMapper.updateReviewByAdmin(update);
        if (rows == 0)
        {
            throw new ServiceException("Review update failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAdminReview(Long reviewId)
    {
        if (reviewId == null)
        {
            throw new ServiceException("Review id is required");
        }
        Map<String, Object> existing = agriMallMapper.selectReviewById(reviewId);
        if (existing == null)
        {
            throw new ServiceException("Review not found");
        }
        int rows = agriMallMapper.deleteReviewById(reviewId);
        if (rows == 0)
        {
            throw new ServiceException("Review delete failed");
        }

        Long itemId = toLong(existing.get("orderItemId"));
        if (itemId == null)
        {
            return;
        }
        Integer remainCount = agriMallMapper.countReviewByOrderItem(itemId);
        if (remainCount == null || remainCount == 0)
        {
            agriMallMapper.updateOrderItemReviewStatusValue(itemId, "0");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(Long buyerId, Map<String, Object> form)
    {
        Long itemId = toLong(form.get("itemId"));
        if (itemId == null)
        {
            throw new ServiceException("Order item is required");
        }
        List<Map<String, Object>> candidates = agriMallMapper.selectReviewableItems(buyerId);
        Map<String, Object> target = candidates.stream().filter(row -> Objects.equals(toLong(row.get("itemId")), itemId)).findFirst().orElse(null);
        if (target == null)
        {
            throw new ServiceException("Order item is not reviewable");
        }

        Date now = new Date();
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("orderId", toLong(target.get("orderId")));
        reviewMap.put("orderItemId", itemId);
        reviewMap.put("productId", toLong(target.get("productId")));
        reviewMap.put("buyerId", buyerId);
        reviewMap.put("farmerId", toLong(target.get("farmerId")));
        reviewMap.put("qualityStar", normalizeStar(form.get("qualityStar")));
        reviewMap.put("packageStar", normalizeStar(form.get("packageStar")));
        reviewMap.put("serviceStar", normalizeStar(form.get("serviceStar")));
        reviewMap.put("content", trimToEmpty(form.get("content")));
        reviewMap.put("createTime", now);
        agriMallMapper.insertReview(reviewMap);
        agriMallMapper.updateOrderItemReviewStatus(itemId);
    }

    @Override
    public Map<String, Object> selectFarmerApplyByUserId(Long userId)
    {
        return agriMallMapper.selectFarmerApplyByUserId(userId);
    }

    @Override
    public void submitFarmerApply(Long userId, Map<String, Object> form)
    {
        Date now = new Date();
        Map<String, Object> existing = agriMallMapper.selectFarmerApplyByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("realName", trimToEmpty(form.get("realName")));
        data.put("idCard", trimToEmpty(form.get("idCard")));
        data.put("phone", trimToEmpty(form.get("phone")));
        data.put("farmName", trimToEmpty(form.get("farmName")));
        data.put("farmAddress", trimToEmpty(form.get("farmAddress")));
        data.put("qualificationImg", trimToEmpty(form.get("qualificationImg")));
        data.put("status", "0");
        data.put("auditBy", "");
        data.put("auditTime", null);
        data.put("auditRemark", "");
        data.put("createTime", now);
        data.put("updateTime", now);
        if (existing == null)
        {
            agriMallMapper.insertFarmerApply(data);
        }
        else
        {
            data.put("applyId", toLong(existing.get("applyId")));
            agriMallMapper.updateFarmerApply(data);
        }
        addAdminNoticeForRole(
            ADMIN_ROLE_KEY,
            "FARMER_APPLY",
            toLong(data.get("applyId")),
            "Farmer application pending",
            "Farm " + trimToEmpty(data.get("farmName")) + " submitted an application",
            now);
    }

    @Override
    public List<Map<String, Object>> selectFarmerApplyList(Map<String, Object> query)
    {
        return agriMallMapper.selectFarmerApplyList(defaultMap(query));
    }

    @Override
    public void auditFarmerApply(String auditBy, Long applyId, String status, String auditRemark)
    {
        if (!("1".equals(status) || "2".equals(status)))
        {
            throw new ServiceException("Invalid apply status");
        }
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("applyId", applyId);
        data.put("status", status);
        data.put("auditBy", auditBy);
        data.put("auditTime", now);
        data.put("auditRemark", trimToEmpty(auditRemark));
        data.put("updateTime", now);
        agriMallMapper.updateFarmerApplyAudit(data);
    }

    @Override
    public List<Map<String, Object>> selectFarmerProductList(Long farmerId, Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        params.put("farmerId", farmerId);
        return agriMallMapper.selectFarmerProductList(params);
    }

    @Override
    public void saveFarmerProduct(Long farmerId, Map<String, Object> form)
    {
        Date now = new Date();
        Long productId = toLong(form.get("productId"));
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("categoryId", toLong(form.get("categoryId")));
        data.put("farmerId", farmerId);
        data.put("productName", trimToEmpty(form.get("productName")));
        data.put("productCover", trimToEmpty(form.get("productCover")));
        data.put("productImages", trimToEmpty(form.get("productImages")));
        data.put("productDesc", trimToEmpty(form.get("productDesc")));
        data.put("price", toBigDecimal(form.get("price")));
        data.put("stock", toInt(form.get("stock"), 0));
        data.put("salesCount", 0);
        data.put("status", "0");
        data.put("auditBy", "");
        data.put("auditTime", null);
        data.put("auditRemark", "");
        data.put("createTime", now);
        data.put("updateTime", now);
        if (productId == null)
        {
            agriMallMapper.insertProduct(data);
        }
        else
        {
            agriMallMapper.updateProductByFarmer(data);
        }
        addAdminNoticeForRole(
            ADMIN_ROLE_KEY,
            "PRODUCT_AUDIT",
            toLong(data.get("productId")),
            "Product audit pending",
            "Product " + trimToEmpty(data.get("productName")) + " submitted for audit",
            now);
    }

    @Override
    public void removeFarmerProduct(Long farmerId, Long productId)
    {
        agriMallMapper.deleteProductByFarmer(productId, farmerId);
    }

    @Override
    public List<Map<String, Object>> selectAdminProductList(Map<String, Object> query)
    {
        return agriMallMapper.selectAdminProductList(defaultMap(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminProduct(String operator, Map<String, Object> form)
    {
        Long productId = toLong(form.get("productId"));
        Date now = new Date();
        Map<String, Object> existing = null;
        if (productId != null)
        {
            existing = agriMallMapper.selectProductDetail(productId);
            if (existing == null)
            {
                throw new ServiceException("Product not found");
            }
        }

        Long farmerId = toLong(form.get("farmerId"));
        if (farmerId == null && existing != null)
        {
            farmerId = toLong(existing.get("farmerId"));
        }
        if (farmerId == null)
        {
            throw new ServiceException("Farmer is required");
        }

        String status = normalizeProductStatus(
            toStr(form.get("status")),
            existing == null ? "1" : toStr(existing.get("status")));
        String auditRemark = trimToEmpty(form.get("auditRemark"));

        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("categoryId", toLong(form.get("categoryId")));
        data.put("farmerId", farmerId);
        data.put("productName", trimToEmpty(form.get("productName")));
        data.put("productCover", trimToEmpty(form.get("productCover")));
        data.put("productImages", trimToEmpty(form.get("productImages")));
        data.put("productDesc", trimToEmpty(form.get("productDesc")));
        data.put("price", toBigDecimal(form.get("price")));
        data.put("stock", toInt(form.get("stock"), 0));
        data.put("salesCount", existing == null ? 0 : toInt(existing.get("salesCount"), 0));
        data.put("status", status);
        data.put("auditBy", "0".equals(status) ? "" : trimToEmpty(operator));
        data.put("auditTime", "0".equals(status) ? null : now);
        data.put("auditRemark", auditRemark);
        data.put("createTime", existing == null ? now : toDate(existing.get("createTime")));
        data.put("updateTime", now);

        if (productId == null)
        {
            agriMallMapper.insertProduct(data);
        }
        else
        {
            int rows = agriMallMapper.updateProductByFarmer(data);
            if (rows == 0)
            {
                throw new ServiceException("Product update failed");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAdminProduct(Long productId)
    {
        if (productId == null)
        {
            throw new ServiceException("Product is required");
        }
        Map<String, Object> existing = agriMallMapper.selectProductDetail(productId);
        if (existing == null)
        {
            throw new ServiceException("Product not found");
        }
        Long farmerId = toLong(existing.get("farmerId"));
        int rows = agriMallMapper.deleteProductByFarmer(productId, farmerId);
        if (rows == 0)
        {
            throw new ServiceException("Product delete failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminProductMedia(String operator, Long productId, String productCover, String productImages)
    {
        if (productId == null)
        {
            throw new ServiceException("Product is required");
        }
        Map<String, Object> existing = agriMallMapper.selectProductDetail(productId);
        if (existing == null)
        {
            throw new ServiceException("Product not found");
        }
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("productCover", trimToEmpty(productCover));
        data.put("productImages", trimToEmpty(productImages));
        data.put("updateTime", now);
        int rows = agriMallMapper.updateProductMedia(data);
        if (rows == 0)
        {
            throw new ServiceException("Product media update failed");
        }
    }

    @Override
    public void auditProduct(String auditBy, Long productId, String status, String auditRemark)
    {
        if (!("1".equals(status) || "2".equals(status) || "3".equals(status)))
        {
            throw new ServiceException("Invalid product status");
        }
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("status", status);
        data.put("auditBy", auditBy);
        data.put("auditTime", now);
        data.put("auditRemark", trimToEmpty(auditRemark));
        data.put("updateTime", now);
        agriMallMapper.updateProductAudit(data);
    }

    @Override
    public Map<String, Object> selectFarmerStats(Long farmerId, String period)
    {
        LocalDate end = LocalDate.now();
        LocalDate begin = "month".equalsIgnoreCase(period) ? end.withDayOfMonth(1) : end;
        Map<String, Object> summary = agriMallMapper.selectFarmerSummary(farmerId, begin.format(DATE_FMT), end.format(DATE_FMT));
        if (summary == null)
        {
            summary = new HashMap<>();
            summary.put("totalOrders", 0);
            summary.put("totalSales", BigDecimal.ZERO);
            summary.put("completedOrders", 0);
        }

        List<Map<String, Object>> rawCurve = agriMallMapper.selectFarmerCurve(farmerId, begin.format(DATE_FMT), end.format(DATE_FMT));
        Map<String, BigDecimal> curveMap = new HashMap<>();
        for (Map<String, Object> row : rawCurve)
        {
            curveMap.put(toStr(row.get("statDate")), toBigDecimal(row.get("salesAmount")));
        }
        List<Map<String, Object>> fullCurve = new ArrayList<>();
        LocalDate cursor = begin;
        while (!cursor.isAfter(end))
        {
            String day = cursor.format(DATE_FMT);
            Map<String, Object> node = new HashMap<>();
            node.put("statDate", day);
            node.put("salesAmount", curveMap.getOrDefault(day, BigDecimal.ZERO));
            fullCurve.add(node);
            cursor = cursor.plusDays(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("curve", fullCurve);
        result.put("hotProducts", agriMallMapper.selectFarmerHotProducts(farmerId));
        result.put("lowStockCount", agriMallMapper.countFarmerLowStock(farmerId, 20));
        return result;
    }

    @Override
    public Map<String, Object> selectAdminStats()
    {
        Map<String, Object> summary = agriMallMapper.selectAdminSummary();
        if (summary == null)
        {
            summary = new HashMap<>();
            summary.put("totalSales", BigDecimal.ZERO);
            summary.put("totalOrders", 0);
            summary.put("totalFarmers", 0);
            summary.put("totalProducts", 0);
        }

        List<Map<String, Object>> categoryDistribution = agriMallMapper.selectAdminCategoryDistribution();
        List<Map<String, Object>> statusDistribution = agriMallMapper.selectAdminOrderStatusDistribution();

        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusMonths(5).withDayOfMonth(1);
        List<Map<String, Object>> rawSalesCurve = agriMallMapper.selectAdminSalesCurve(begin.format(DATE_FMT), end.format(DATE_FMT));
        Map<String, BigDecimal> salesMap = new HashMap<>();
        for (Map<String, Object> row : rawSalesCurve)
        {
            salesMap.put(toStr(row.get("statMonth")), toBigDecimal(row.get("salesAmount")));
        }

        List<Map<String, Object>> fullSalesCurve = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate monthCursor = begin;
        while (!monthCursor.isBefore(end.withDayOfMonth(1)) || monthCursor.isBefore(end.plusMonths(1).withDayOfMonth(1)))
        {
            if (monthCursor.isAfter(end.withDayOfMonth(1))) break;
            String month = monthCursor.format(monthFmt);
            Map<String, Object> node = new HashMap<>();
            node.put("statMonth", month);
            node.put("salesAmount", salesMap.getOrDefault(month, BigDecimal.ZERO));
            fullSalesCurve.add(node);
            monthCursor = monthCursor.plusMonths(1);
        }

        List<Map<String, Object>> latestNotices = agriMallMapper.selectLatestNotices(5);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("categoryDistribution", categoryDistribution);
        result.put("statusDistribution", statusDistribution);
        result.put("salesCurve", fullSalesCurve);
        result.put("notices", latestNotices);
        return result;
    }

    @Override
    public void createDispute(Long buyerId, Long orderId, String title, String content)
    {
        if (orderId == null)
        {
            throw new ServiceException("订单ID不能为空");
        }
        String contentText = trimToEmpty(content);
        if (StringUtils.isEmpty(contentText))
        {
            throw new ServiceException("纠纷内容不能为空");
        }
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("订单不存在或无权限");
        }
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("buyerId", buyerId);
        data.put("farmerId", toLong(order.get("farmer_id")));
        data.put("title", StringUtils.isEmpty(trimToEmpty(title)) ? "订单纠纷" : trimToEmpty(title));
        data.put("content", contentText);
        data.put("status", "0");
        data.put("handleResult", "");
        data.put("handleBy", "");
        data.put("handleTime", null);
        data.put("createTime", now);
        data.put("updateTime", now);
        agriMallMapper.insertDispute(data);
        addAdminNoticeForRole(
            ADMIN_ROLE_KEY,
            "DISPUTE",
            toLong(data.get("disputeId")),
            "交易纠纷待处理",
            "订单 " + toStr(order.get("order_no")) + " 发起纠纷：" + trimToEmpty(data.get("title")),
            now);
    }

    @Override
    public List<Map<String, Object>> selectBuyerDisputeList(Long buyerId, Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        params.put("buyerId", buyerId);
        return agriMallMapper.selectDisputeList(params);
    }

    @Override
    public List<Map<String, Object>> selectDisputeList(Map<String, Object> query)
    {
        return agriMallMapper.selectDisputeList(defaultMap(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminDispute(String operator, Map<String, Object> form)
    {
        Map<String, Object> request = defaultMap(form);
        Long disputeId = toLong(request.get("disputeId"));
        Long orderId = toLong(request.get("orderId"));
        String title = trimToEmpty(request.get("title"));
        String content = trimToEmpty(request.get("content"));
        String status = trimToEmpty(request.get("status"));
        String handleResult = trimToEmpty(request.get("handleResult"));

        if (StringUtils.isEmpty(status))
        {
            status = "0";
        }
        if (!("0".equals(status) || "1".equals(status) || "2".equals(status)))
        {
            throw new ServiceException("Invalid dispute status");
        }

        Date now = new Date();
        if (disputeId == null)
        {
            if (orderId == null)
            {
                throw new ServiceException("Order id is required");
            }
            Map<String, Object> order = agriMallMapper.selectOrderById(orderId);
            if (order == null)
            {
                throw new ServiceException("Order not found");
            }
            if (StringUtils.isEmpty(title))
            {
                title = "订单咨询";
            }
            if (StringUtils.isEmpty(content))
            {
                throw new ServiceException("Consult content is required");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", orderId);
            data.put("buyerId", toLong(order.get("buyer_id")));
            data.put("farmerId", toLong(order.get("farmer_id")));
            data.put("title", title);
            data.put("content", content);
            data.put("status", status);
            data.put("handleResult", handleResult);
            data.put("handleBy", StringUtils.isEmpty(handleResult) ? "" : trimToEmpty(operator));
            data.put("handleTime", StringUtils.isEmpty(handleResult) ? null : now);
            data.put("createTime", now);
            data.put("updateTime", now);
            agriMallMapper.insertDispute(data);
            return;
        }

        Map<String, Object> existing = agriMallMapper.selectDisputeById(disputeId);
        if (existing == null)
        {
            throw new ServiceException("Consult not found");
        }

        Long targetOrderId = orderId == null ? toLong(existing.get("order_id")) : orderId;
        Map<String, Object> order = agriMallMapper.selectOrderById(targetOrderId);
        if (order == null)
        {
            throw new ServiceException("Order not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("disputeId", disputeId);
        data.put("orderId", targetOrderId);
        data.put("buyerId", toLong(order.get("buyer_id")));
        data.put("farmerId", toLong(order.get("farmer_id")));
        data.put("title", StringUtils.isEmpty(title) ? toStr(existing.get("title")) : title);
        data.put("content", StringUtils.isEmpty(content) ? toStr(existing.get("content")) : content);
        data.put("status", status);
        data.put("handleResult", handleResult);
        data.put("handleBy", StringUtils.isEmpty(handleResult) ? toStr(existing.get("handle_by")) : trimToEmpty(operator));
        data.put("handleTime", StringUtils.isEmpty(handleResult) ? toDate(existing.get("handle_time")) : now);
        data.put("updateTime", now);
        agriMallMapper.updateDisputeByAdmin(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAdminDispute(Long disputeId)
    {
        if (disputeId == null)
        {
            throw new ServiceException("Consult id is required");
        }
        agriMallMapper.deleteDisputeById(disputeId);
    }

    @Override
    public void handleDispute(String handleBy, Long disputeId, String status, String handleResult)
    {
        if (disputeId == null)
        {
            throw new ServiceException("纠纷ID不能为空");
        }
        String targetStatus = StringUtils.isEmpty(trimToEmpty(status)) ? "2" : trimToEmpty(status);
        if (!("1".equals(targetStatus) || "2".equals(targetStatus)))
        {
            throw new ServiceException("纠纷状态不合法");
        }
        String resultHtml = trimToEmpty(handleResult);
        String resultText = resultHtml.replaceAll("<[^>]*>", " ").replace("&nbsp;", " ").trim();
        if ("2".equals(targetStatus) && StringUtils.isEmpty(resultText))
        {
            throw new ServiceException("处理结果不能为空");
        }
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("disputeId", disputeId);
        data.put("status", targetStatus);
        data.put("handleResult", resultHtml);
        data.put("handleBy", handleBy);
        data.put("handleTime", now);
        data.put("updateTime", now);
        agriMallMapper.updateDisputeHandle(data);
    }

    @Override
    public List<Map<String, Object>> selectAdminNoticeList(Long adminId, Map<String, Object> query)
    {
        Map<String, Object> params = defaultMap(query);
        params.put("adminId", adminId);
        return agriMallMapper.selectAdminNoticeList(params);
    }

    @Override
    public int countAdminUnreadNotice(Long adminId)
    {
        Integer count = agriMallMapper.countAdminUnreadNotice(adminId);
        return count == null ? 0 : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readAdminNotice(Long adminId, Long noticeId)
    {
        if (noticeId == null)
        {
            throw new ServiceException("Notice id is required");
        }
        int rows = agriMallMapper.updateAdminNoticeRead(noticeId, adminId, new Date());
        if (rows == 0)
        {
            throw new ServiceException("Notice not found");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int readAllAdminNotice(Long adminId)
    {
        return agriMallMapper.updateAdminNoticeReadAll(adminId, new Date());
    }

    @Override
    public List<Map<String, Object>> selectFreightTemplateList(Map<String, Object> query)
    {
        return agriMallMapper.selectFreightTemplateList(defaultMap(query));
    }

    @Override
    public void saveFreightTemplate(String operator, Map<String, Object> form)
    {
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("templateId", toLong(form.get("templateId")));
        data.put("templateName", trimToEmpty(form.get("templateName")));
        data.put("regionCode", trimToEmpty(form.get("regionCode")));
        data.put("firstFee", toBigDecimal(form.get("firstFee")));
        data.put("extraFee", toBigDecimal(form.get("extraFee")));
        data.put("status", StringUtils.isEmpty(toStr(form.get("status"))) ? "0" : toStr(form.get("status")));
        data.put("createBy", operator);
        data.put("createTime", now);
        data.put("updateBy", operator);
        data.put("updateTime", now);
        data.put("remark", trimToEmpty(form.get("remark")));
        if (toLong(form.get("templateId")) == null)
        {
            agriMallMapper.insertFreightTemplate(data);
        }
        else
        {
            agriMallMapper.updateFreightTemplate(data);
        }
    }

    @Override
    public void removeFreightTemplate(Long templateId)
    {
        agriMallMapper.deleteFreightTemplate(templateId);
    }

    @Override
    public List<Map<String, Object>> selectBuyerCouponList(Long buyerId, BigDecimal orderAmount)
    {
        List<Map<String, Object>> couponList = agriMallMapper.selectBuyerCouponList(buyerId);
        BigDecimal amount = orderAmount == null ? BigDecimal.ZERO : orderAmount;
        Date now = new Date();
        for (Map<String, Object> coupon : couponList)
        {
            boolean active = isCouponActive(coupon, now);
            BigDecimal preview = active ? calculateCouponDiscount(coupon, amount) : BigDecimal.ZERO;
            coupon.put("discountPreview", preview);
            coupon.put("availableFlag", active && preview.compareTo(BigDecimal.ZERO) > 0 ? "1" : "0");
        }
        return couponList;
    }

    @Override
    public List<Map<String, Object>> selectBuyerActivePromotionList()
    {
        return loadActivePromotions();
    }

    @Override
    public List<Map<String, Object>> selectCouponList(Map<String, Object> query)
    {
        return agriMallMapper.selectCouponList(defaultMap(query));
    }

    @Override
    public void saveCoupon(String operator, Map<String, Object> form)
    {
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("couponId", toLong(form.get("couponId")));
        data.put("couponName", trimToEmpty(form.get("couponName")));
        data.put("couponType", StringUtils.isEmpty(toStr(form.get("couponType"))) ? "1" : toStr(form.get("couponType")));
        data.put("thresholdAmount", toBigDecimal(form.get("thresholdAmount")));
        data.put("discountAmount", toBigDecimal(form.get("discountAmount")));
        data.put("discountRate", toBigDecimal(form.get("discountRate")));
        data.put("startTime", form.get("startTime"));
        data.put("endTime", form.get("endTime"));
        data.put("totalCount", toInt(form.get("totalCount"), 0));
        data.put("remainCount", toInt(form.get("remainCount"), 0));
        data.put("status", StringUtils.isEmpty(toStr(form.get("status"))) ? "0" : toStr(form.get("status")));
        data.put("createBy", operator);
        data.put("createTime", now);
        data.put("updateBy", operator);
        data.put("updateTime", now);
        data.put("remark", trimToEmpty(form.get("remark")));
        if (toLong(form.get("couponId")) == null)
        {
            agriMallMapper.insertCoupon(data);
        }
        else
        {
            agriMallMapper.updateCoupon(data);
        }
    }

    @Override
    public void removeCoupon(Long couponId)
    {
        agriMallMapper.deleteCoupon(couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueCoupon(String operator, Long couponId, Long[] userIds)
    {
        if (couponId == null)
        {
            throw new ServiceException("Coupon is required");
        }
        if (userIds == null || userIds.length == 0)
        {
            throw new ServiceException("Please select users");
        }

        LinkedHashSet<Long> uniqueUserIds = new LinkedHashSet<>();
        for (Long userId : userIds)
        {
            if (userId != null && userId > 0)
            {
                uniqueUserIds.add(userId);
            }
        }
        if (uniqueUserIds.isEmpty())
        {
            throw new ServiceException("Please select users");
        }

        Map<String, Object> query = new HashMap<>();
        query.put("couponId", couponId);
        List<Map<String, Object>> coupons = agriMallMapper.selectCouponList(query);
        if (coupons == null || coupons.isEmpty())
        {
            throw new ServiceException("Coupon not found");
        }
        Map<String, Object> coupon = coupons.get(0);
        if (!"0".equals(toStr(coupon.get("status"))))
        {
            throw new ServiceException("Coupon is disabled");
        }

        Date now = new Date();
        int deductRows = agriMallMapper.decreaseCouponRemain(couponId, uniqueUserIds.size(), trimToEmpty(operator), now);
        if (deductRows == 0)
        {
            throw new ServiceException("Coupon stock is insufficient");
        }

        for (Long userId : uniqueUserIds)
        {
            Map<String, Object> data = new HashMap<>();
            data.put("couponId", couponId);
            data.put("userId", userId);
            data.put("status", "0");
            data.put("grantBy", trimToEmpty(operator));
            data.put("grantTime", now);
            data.put("usedTime", null);
            data.put("usedOrderId", null);
            data.put("discountAmount", BigDecimal.ZERO);
            data.put("createTime", now);
            data.put("updateTime", now);
            data.put("remark", "");
            agriMallMapper.insertUserCoupon(data);
        }
    }

    @Override
    public List<Map<String, Object>> selectPromotionList(Map<String, Object> query)
    {
        return agriMallMapper.selectPromotionList(defaultMap(query));
    }

    @Override
    public void savePromotion(String operator, Map<String, Object> form)
    {
        Date now = new Date();
        Map<String, Object> data = new HashMap<>();
        data.put("promotionId", toLong(form.get("promotionId")));
        data.put("promotionName", trimToEmpty(form.get("promotionName")));
        data.put("promotionType", StringUtils.isEmpty(toStr(form.get("promotionType"))) ? "1" : toStr(form.get("promotionType")));
        data.put("startTime", form.get("startTime"));
        data.put("endTime", form.get("endTime"));
        data.put("content", trimToEmpty(form.get("content")));
        data.put("status", StringUtils.isEmpty(toStr(form.get("status"))) ? "0" : toStr(form.get("status")));
        data.put("createBy", operator);
        data.put("createTime", now);
        data.put("updateBy", operator);
        data.put("updateTime", now);
        data.put("remark", trimToEmpty(form.get("remark")));
        if (toLong(form.get("promotionId")) == null)
        {
            agriMallMapper.insertPromotion(data);
        }
        else
        {
            agriMallMapper.updatePromotion(data);
        }
    }

    @Override
    public void removePromotion(Long promotionId)
    {
        agriMallMapper.deletePromotion(promotionId);
    }

    @Override
    public List<Map<String, Object>> selectBuyerAddressList(Long userId)
    {
        return agriMallMapper.selectBuyerAddressList(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBuyerAddress(Long userId, Map<String, Object> form)
    {
        Long addressId = toLong(form.get("addressId"));
        String isDefault = "1".equals(toStr(form.get("isDefault"))) ? "1" : "0";
        Date now = new Date();

        if ("1".equals(isDefault))
        {
            agriMallMapper.clearBuyerDefaultAddress(userId, now);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("addressId", addressId);
        data.put("userId", userId);
        data.put("receiverName", trimToEmpty(form.get("receiverName")));
        data.put("receiverPhone", trimToEmpty(form.get("receiverPhone")));
        data.put("receiverAddress", trimToEmpty(form.get("receiverAddress")));
        data.put("isDefault", isDefault);
        data.put("createTime", now);
        data.put("updateTime", now);

        if (addressId == null)
        {
            agriMallMapper.insertBuyerAddress(data);
        }
        else
        {
            agriMallMapper.updateBuyerAddress(data);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBuyerAddress(Long userId, Long addressId)
    {
        agriMallMapper.deleteBuyerAddress(addressId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId)
    {
        Date now = new Date();
        agriMallMapper.clearBuyerDefaultAddress(userId, now);
        agriMallMapper.setBuyerDefaultAddress(addressId, userId, now);
    }

    private Map<Long, BigDecimal> splitDiscount(BigDecimal totalAmount, BigDecimal totalDiscount, Map<Long, BigDecimal> orderAmountMap)
    {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        if (orderAmountMap == null || orderAmountMap.isEmpty())
        {
            return result;
        }
        BigDecimal discount = totalDiscount == null ? BigDecimal.ZERO : totalDiscount;
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        if (discount.compareTo(BigDecimal.ZERO) <= 0 || total.compareTo(BigDecimal.ZERO) <= 0)
        {
            for (Long farmerId : orderAmountMap.keySet())
            {
                result.put(farmerId, BigDecimal.ZERO);
            }
            return result;
        }

        BigDecimal remaining = discount;
        List<Map.Entry<Long, BigDecimal>> entries = new ArrayList<>(orderAmountMap.entrySet());
        for (int i = 0; i < entries.size(); i++)
        {
            Map.Entry<Long, BigDecimal> entry = entries.get(i);
            BigDecimal orderAmount = entry.getValue() == null ? BigDecimal.ZERO : entry.getValue();
            BigDecimal share;
            if (i == entries.size() - 1)
            {
                share = remaining;
            }
            else
            {
                share = discount.multiply(orderAmount).divide(total, 2, RoundingMode.HALF_UP);
                if (share.compareTo(remaining) > 0)
                {
                    share = remaining;
                }
            }
            if (share.compareTo(orderAmount) > 0)
            {
                share = orderAmount;
            }
            if (share.compareTo(BigDecimal.ZERO) < 0)
            {
                share = BigDecimal.ZERO;
            }
            remaining = remaining.subtract(share);
            if (remaining.compareTo(BigDecimal.ZERO) < 0)
            {
                remaining = BigDecimal.ZERO;
            }
            result.put(entry.getKey(), share);
        }
        return result;
    }

    private List<Map<String, Object>> loadActivePromotions()
    {
        List<Map<String, Object>> list = agriMallMapper.selectActivePromotionList();
        if (list == null || list.isEmpty())
        {
            return Collections.emptyList();
        }
        Date now = new Date();
        return list.stream().filter(item -> isPromotionActive(item, now)).collect(Collectors.toList());
    }

    private void applyPromotionPriceToList(List<Map<String, Object>> items, List<Map<String, Object>> promotions)
    {
        if (items == null || items.isEmpty())
        {
            return;
        }
        for (Map<String, Object> item : items)
        {
            applyPromotionPriceToItem(item, promotions);
        }
    }

    private void applyPromotionPriceToItem(Map<String, Object> item, List<Map<String, Object>> promotions)
    {
        if (item == null)
        {
            return;
        }
        BigDecimal originPrice = toBigDecimal(item.get("price")).setScale(2, RoundingMode.HALF_UP);
        PromotionApplyResult result = bestPromotionResult(originPrice, promotions);
        item.put("originPrice", originPrice);
        item.put("promotionPrice", result.price);
        item.put("promotionDiscount", result.discount);
        item.put("price", result.price);
        Map<String, Object> appliedPromotion = result.promotion;
        if (appliedPromotion == null && promotions != null && !promotions.isEmpty())
        {
            appliedPromotion = promotions.get(0);
        }
        if (appliedPromotion != null)
        {
            item.put("promotionName", trimToEmpty(appliedPromotion.get("promotionName")));
            item.put("promotionType", trimToEmpty(appliedPromotion.get("promotionType")));
        }
        else
        {
            item.put("promotionName", "");
            item.put("promotionType", "");
        }
    }

    private PromotionApplyResult bestPromotionResult(BigDecimal originPrice, List<Map<String, Object>> promotions)
    {
        BigDecimal basePrice = originPrice == null ? BigDecimal.ZERO : originPrice.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        PromotionApplyResult best = new PromotionApplyResult(basePrice, BigDecimal.ZERO, null);
        if (promotions == null || promotions.isEmpty() || basePrice.compareTo(BigDecimal.ZERO) <= 0)
        {
            return best;
        }
        for (Map<String, Object> promotion : promotions)
        {
            BigDecimal discount = calculatePromotionDiscount(basePrice, promotion);
            if (discount.compareTo(best.discount) > 0)
            {
                BigDecimal currentPrice = basePrice.subtract(discount);
                if (currentPrice.compareTo(BigDecimal.ZERO) < 0)
                {
                    currentPrice = BigDecimal.ZERO;
                }
                best = new PromotionApplyResult(currentPrice.setScale(2, RoundingMode.HALF_UP), discount.setScale(2, RoundingMode.HALF_UP), promotion);
            }
        }
        return best;
    }

    private BigDecimal calculatePromotionDiscount(BigDecimal price, Map<String, Object> promotion)
    {
        if (promotion == null || price == null || price.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        String content = trimToEmpty(promotion.get("content"));
        String promotionType = trimToEmpty(promotion.get("promotionType"));
        BigDecimal maxDiscount = BigDecimal.ZERO;

        BigDecimal discountRate = parseDiscountRate(content);
        if (discountRate.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal discountByRate = price.subtract(price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP));
            if (discountByRate.compareTo(maxDiscount) > 0)
            {
                maxDiscount = discountByRate;
            }
        }

        BigDecimal[] fullReduce = parseFullReduce(content);
        if (fullReduce[0].compareTo(BigDecimal.ZERO) > 0
            && fullReduce[1].compareTo(BigDecimal.ZERO) > 0
            && price.compareTo(fullReduce[0]) >= 0
            && fullReduce[1].compareTo(maxDiscount) > 0)
        {
            maxDiscount = fullReduce[1];
        }

        BigDecimal directReduce = parseDirectReduce(content);
        if (directReduce.compareTo(BigDecimal.ZERO) > 0 && directReduce.compareTo(maxDiscount) > 0)
        {
            maxDiscount = directReduce;
        }

        // Backward compatibility: old records may only set type with no explicit discount text.
        if (maxDiscount.compareTo(BigDecimal.ZERO) <= 0 && "1".equals(promotionType))
        {
            maxDiscount = price.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        }

        if (maxDiscount.compareTo(price) > 0)
        {
            maxDiscount = price;
        }
        if (maxDiscount.compareTo(BigDecimal.ZERO) < 0)
        {
            maxDiscount = BigDecimal.ZERO;
        }
        return maxDiscount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal parseDiscountRate(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return BigDecimal.ZERO;
        }
        Matcher foldMatcher = PROMOTION_FOLD_PATTERN.matcher(content);
        if (foldMatcher.find())
        {
            BigDecimal fold = toBigDecimal(foldMatcher.group(1));
            if (fold.compareTo(BigDecimal.ZERO) > 0 && fold.compareTo(BigDecimal.TEN) < 0)
            {
                return fold.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP);
            }
        }

        Matcher percentMatcher = PROMOTION_PERCENT_PATTERN.matcher(content);
        if (percentMatcher.find())
        {
            BigDecimal percent = toBigDecimal(percentMatcher.group(1));
            if (percent.compareTo(BigDecimal.ZERO) > 0 && percent.compareTo(new BigDecimal("100")) < 0)
            {
                return percent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal[] parseFullReduce(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
        }
        Matcher matcher = PROMOTION_FULL_REDUCE_PATTERN.matcher(content);
        if (!matcher.find())
        {
            return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
        }
        return new BigDecimal[] { toBigDecimal(matcher.group(1)), toBigDecimal(matcher.group(2)) };
    }

    private BigDecimal parseDirectReduce(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return BigDecimal.ZERO;
        }
        Matcher matcher = PROMOTION_DIRECT_REDUCE_PATTERN.matcher(content);
        if (!matcher.find())
        {
            return BigDecimal.ZERO;
        }
        return toBigDecimal(matcher.group(1));
    }

    private boolean isPromotionActive(Map<String, Object> promotion, Date now)
    {
        if (promotion == null)
        {
            return false;
        }
        if (!"0".equals(trimToEmpty(promotion.get("status"))))
        {
            return false;
        }
        Date current = now == null ? new Date() : now;
        Date startTime = toDate(promotion.get("startTime"));
        Date endTime = toDate(promotion.get("endTime"));
        if (startTime != null && current.before(startTime))
        {
            return false;
        }
        return endTime == null || !current.after(endTime);
    }

    private static class PromotionApplyResult
    {
        private final BigDecimal price;
        private final BigDecimal discount;
        private final Map<String, Object> promotion;

        private PromotionApplyResult(BigDecimal price, BigDecimal discount, Map<String, Object> promotion)
        {
            this.price = price;
            this.discount = discount;
            this.promotion = promotion;
        }
    }

    private BigDecimal calculateCouponDiscount(Map<String, Object> coupon, BigDecimal orderAmount)
    {
        if (coupon == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return BigDecimal.ZERO;
        }
        String couponType = toStr(coupon.get("couponType"));
        BigDecimal thresholdAmount = toBigDecimal(coupon.get("thresholdAmount"));
        if (thresholdAmount.compareTo(BigDecimal.ZERO) > 0 && orderAmount.compareTo(thresholdAmount) < 0)
        {
            return BigDecimal.ZERO;
        }
        if ("1".equals(couponType))
        {
            BigDecimal discount = toBigDecimal(coupon.get("discountAmount"));
            if (discount.compareTo(BigDecimal.ZERO) <= 0)
            {
                return BigDecimal.ZERO;
            }
            return discount.compareTo(orderAmount) > 0 ? orderAmount : discount;
        }
        if ("2".equals(couponType))
        {
            BigDecimal discountRate = toBigDecimal(coupon.get("discountRate"));
            if (discountRate.compareTo(BigDecimal.ZERO) <= 0 || discountRate.compareTo(BigDecimal.TEN) >= 0)
            {
                return BigDecimal.ZERO;
            }
            BigDecimal discount = orderAmount.multiply(BigDecimal.TEN.subtract(discountRate)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
            if (discount.compareTo(BigDecimal.ZERO) <= 0)
            {
                return BigDecimal.ZERO;
            }
            return discount.compareTo(orderAmount) > 0 ? orderAmount : discount;
        }
        return BigDecimal.ZERO;
    }

    private boolean isCouponActive(Map<String, Object> coupon, Date now)
    {
        if (coupon == null)
        {
            return false;
        }
        if (!"0".equals(toStr(coupon.get("status"))))
        {
            return false;
        }
        Date current = now == null ? new Date() : now;
        Date startTime = toDate(coupon.get("startTime"));
        Date endTime = toDate(coupon.get("endTime"));
        if (startTime != null && current.before(startTime))
        {
            return false;
        }
        return endTime == null || !current.after(endTime);
    }

    private void addTrace(Long orderId, String node, String desc, Date time)
    {
        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put("orderId", orderId);
        traceMap.put("statusNode", node);
        traceMap.put("statusDesc", desc);
        traceMap.put("traceTime", time);
        agriMallMapper.insertLogisticsTrace(traceMap);
    }

    private List<Map<String, Object>> buildOrderTrace(Long orderId, Map<String, Object> order)
    {
        List<Map<String, Object>> traceList = new ArrayList<>(agriMallMapper.selectLogisticsByOrder(orderId));
        appendVirtualTraceIfMissing(traceList, "ORDER_CREATED", "Order created and waiting for payment", toDate(order.get("create_time")));
        if ("1".equals(toStr(order.get("pay_status"))) || "2".equals(toStr(order.get("pay_status"))))
        {
            appendVirtualTraceIfMissing(traceList, "PAID", "Payment completed and waiting for farmer handling", toDate(order.get("pay_time")));
        }
        String orderStatus = toStr(order.get("order_status"));
        Date shipTime = toDate(order.get("ship_time"));
        if ("1".equals(orderStatus) || "2".equals(orderStatus) || "3".equals(orderStatus) || "4".equals(orderStatus))
        {
            appendVirtualTraceIfMissing(traceList, "ACCEPTED", "Farmer accepted order and is preparing shipment", toDate(order.get("update_time")));
        }
        if ("2".equals(orderStatus) || "3".equals(orderStatus) || "4".equals(orderStatus))
        {
            appendVirtualTraceIfMissing(traceList, "SHIPPED", "Order shipped from warehouse", shipTime);
            appendVirtualTraceIfMissing(traceList, "IN_TRANSIT", "Order is in transit", afterMinutes(shipTime, 120));
        }
        if ("3".equals(orderStatus) || "4".equals(orderStatus))
        {
            appendVirtualTraceIfMissing(traceList, "OUT_FOR_DELIVERY", "Package arrived at local station and is out for delivery", afterMinutes(shipTime, 240));
        }
        if ("4".equals(orderStatus))
        {
            appendVirtualTraceIfMissing(traceList, "RECEIVED", "Buyer confirmed receipt", toDate(order.get("finish_time")));
        }
        traceList.sort((a, b) -> {
            Date ad = toDate(a.get("traceTime"));
            Date bd = toDate(b.get("traceTime"));
            long av = ad == null ? 0L : ad.getTime();
            long bv = bd == null ? 0L : bd.getTime();
            return Long.compare(bv, av);
        });
        return traceList;
    }

    private Map<String, Object> buildOrderVoucher(Long orderId, Map<String, Object> order)
    {
        Map<String, Object> voucher = agriMallMapper.selectPaymentVoucherByOrder(orderId);
        if (voucher == null)
        {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("orderId", orderId);
            fallback.put("payNo", toStr(order.get("pay_voucher_no")));
            fallback.put("channel", toStr(order.get("payment_channel")));
            fallback.put("channelName", channelName(toStr(order.get("payment_channel"))));
            fallback.put("voucherImg", "");
            fallback.put("payAmount", toBigDecimal(order.get("pay_amount")));
            fallback.put("payTime", order.get("pay_time"));
            fallback.put("status", toStr(order.get("pay_status")));
            return fallback;
        }
        voucher.put("channelName", channelName(toStr(voucher.get("channel"))));
        return voucher;
    }

    private List<Map<String, Object>> enrichOrderFreightMeta(List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return rows == null ? new ArrayList<>() : rows;
        }
        List<Map<String, Object>> templates = agriMallMapper.selectFreightTemplateList(new HashMap<>());
        for (Map<String, Object> row : rows)
        {
            String receiverAddress = trimToEmpty(row.get("receiverAddress"));
            String regionCode = detectRegionCodeByAddress(receiverAddress);
            Map<String, Object> template = matchFreightTemplate(regionCode, templates);
            row.put("freightRegionCode", StringUtils.isEmpty(regionCode) ? "-" : regionCode);
            row.put("freightRegionName", regionNameByCode(regionCode));
            row.put("freightTemplateId", template == null ? null : toLong(template.get("templateId")));
            row.put("freightTemplateName", template == null ? "默认免运费" : trimToEmpty(template.get("templateName")));
        }
        return rows;
    }

    private Map<String, Object> calculateFreightResult(String regionCodeInput, Map<Long, List<Map<String, Object>>> groupedItems)
    {
        List<Map<String, Object>> templates = agriMallMapper.selectFreightTemplateList(new HashMap<>());
        String regionCode = canonicalRegionCode(regionCodeInput);
        Map<String, Object> template = matchFreightTemplate(regionCode, templates);

        Map<Long, BigDecimal> orderFreightMap = new LinkedHashMap<>();
        List<Map<String, Object>> orderFreightList = new ArrayList<>();
        BigDecimal totalFreight = BigDecimal.ZERO;

        if (groupedItems != null && !groupedItems.isEmpty())
        {
            for (Map.Entry<Long, List<Map<String, Object>>> entry : groupedItems.entrySet())
            {
                int qty = (entry.getValue() == null ? Collections.<Map<String, Object>>emptyList() : entry.getValue()).stream()
                    .mapToInt(item -> toInt(item.get("quantity"), 0))
                    .sum();
                BigDecimal orderFreight = calculateOrderFreightByQuantity(template, qty);
                orderFreightMap.put(entry.getKey(), orderFreight);
                totalFreight = totalFreight.add(orderFreight);

                Map<String, Object> detail = new HashMap<>();
                detail.put("farmerId", entry.getKey());
                detail.put("quantity", qty);
                detail.put("freightAmount", orderFreight);
                orderFreightList.add(detail);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("regionCode", StringUtils.isEmpty(regionCode) ? "-" : regionCode);
        result.put("regionName", regionNameByCode(regionCode));
        result.put("templateId", template == null ? null : toLong(template.get("templateId")));
        result.put("templateName", template == null ? "默认免运费" : trimToEmpty(template.get("templateName")));
        result.put("firstFee", template == null ? BigDecimal.ZERO : toBigDecimal(template.get("firstFee")));
        result.put("extraFee", template == null ? BigDecimal.ZERO : toBigDecimal(template.get("extraFee")));
        result.put("freightAmount", totalFreight.setScale(2, RoundingMode.HALF_UP));
        result.put("orderCount", groupedItems == null ? 0 : groupedItems.size());
        result.put("orderFreightList", orderFreightList);
        result.put("orderFreightMap", orderFreightMap);
        return result;
    }

    private String resolveFreightRegionCode(Map<String, Object> request, String receiverAddress)
    {
        Map<String, Object> params = defaultMap(request);
        String explicit = canonicalRegionCode(params.get("receiverRegionCode"));
        if (!StringUtils.isEmpty(explicit))
        {
            return explicit;
        }
        String byAreaCode = detectRegionCodeByAreaCode(
            params.get("receiverProvinceCode"),
            params.get("receiverCityCode"),
            params.get("receiverDistrictCode"));
        if (!StringUtils.isEmpty(byAreaCode))
        {
            return byAreaCode;
        }
        return canonicalRegionCode(detectRegionCodeByAddress(receiverAddress));
    }

    private String detectRegionCodeByAreaCode(Object provinceCodeValue, Object cityCodeValue, Object districtCodeValue)
    {
        String prefix = extractAreaPrefix(provinceCodeValue);
        if (StringUtils.isEmpty(prefix))
        {
            prefix = extractAreaPrefix(cityCodeValue);
        }
        if (StringUtils.isEmpty(prefix))
        {
            prefix = extractAreaPrefix(districtCodeValue);
        }
        if (StringUtils.isEmpty(prefix))
        {
            return "";
        }
        if (Arrays.asList("11", "12", "13", "14", "15").contains(prefix))
        {
            return REGION_NORTH;
        }
        if (Arrays.asList("31", "32", "33", "34", "35", "36", "37").contains(prefix))
        {
            return REGION_EAST;
        }
        if (Arrays.asList("41", "42", "43").contains(prefix))
        {
            return REGION_CENTRAL;
        }
        if (Arrays.asList("44", "45", "46").contains(prefix))
        {
            return REGION_SOUTH;
        }
        if (Arrays.asList("21", "22", "23").contains(prefix))
        {
            return REGION_NORTHEAST;
        }
        if (Arrays.asList("50", "51", "52", "53", "54").contains(prefix))
        {
            return REGION_SOUTHWEST;
        }
        if (Arrays.asList("61", "62", "63", "64", "65").contains(prefix))
        {
            return REGION_NORTHWEST;
        }
        if (Arrays.asList("71", "81", "82").contains(prefix))
        {
            return REGION_HK_MO_TW;
        }
        return "";
    }

    private String extractAreaPrefix(Object areaCodeValue)
    {
        String raw = trimToEmpty(areaCodeValue);
        if (StringUtils.isEmpty(raw))
        {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < 2)
        {
            return "";
        }
        return digits.substring(0, 2);
    }

    private BigDecimal calculateOrderFreightByQuantity(Map<String, Object> template, int quantity)
    {
        if (template == null)
        {
            return BigDecimal.ZERO;
        }
        int chargeQty = Math.max(quantity, 1);
        BigDecimal firstFee = toBigDecimal(template.get("firstFee"));
        BigDecimal extraFee = toBigDecimal(template.get("extraFee"));
        BigDecimal freight = firstFee;
        if (chargeQty > 1 && extraFee.compareTo(BigDecimal.ZERO) > 0)
        {
            freight = freight.add(extraFee.multiply(BigDecimal.valueOf(chargeQty - 1L)));
        }
        return freight.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> matchFreightTemplate(String regionCode, List<Map<String, Object>> templates)
    {
        if (templates == null || templates.isEmpty())
        {
            return null;
        }
        String target = normalizeRegionToken(regionCode);
        List<String> aliases = regionAliasTokens(regionCode);

        for (Map<String, Object> template : templates)
        {
            if (!"0".equals(toStr(template.get("status"))))
            {
                continue;
            }
            String code = normalizeRegionToken(template.get("regionCode"));
            if (StringUtils.isEmpty(code))
            {
                continue;
            }
            if (!StringUtils.isEmpty(target) && code.equals(target))
            {
                return template;
            }
            for (String alias : aliases)
            {
                if (code.equals(alias))
                {
                    return template;
                }
            }
        }

        for (Map<String, Object> template : templates)
        {
            if (!"0".equals(toStr(template.get("status"))))
            {
                continue;
            }
            String code = normalizeRegionToken(template.get("regionCode"));
            if (StringUtils.isEmpty(code))
            {
                continue;
            }
            if (!StringUtils.isEmpty(target) && (code.contains(target) || target.contains(code)))
            {
                return template;
            }
            for (String alias : aliases)
            {
                if (code.contains(alias) || alias.contains(code))
                {
                    return template;
                }
            }
        }

        for (Map<String, Object> template : templates)
        {
            if (!"0".equals(toStr(template.get("status"))))
            {
                continue;
            }
            String code = normalizeRegionToken(template.get("regionCode"));
            if ("ALL".equals(code) || "CNALL".equals(code) || "DEFAULT".equals(code) || "NATIONWIDE".equals(code) || code.contains("全国"))
            {
                return template;
            }
        }
        return null;
    }

    private List<String> regionAliasTokens(String regionCode)
    {
        String code = trimToEmpty(regionCode);
        if (StringUtils.isEmpty(code))
        {
            return Collections.emptyList();
        }
        if (REGION_EAST.equals(code))
        {
            return Arrays.asList("CNEAST", "华东", "华东地区", "HUADONG", "EAST", "EASTCHINA");
        }
        if (REGION_CENTRAL.equals(code))
        {
            return Arrays.asList("CNCENTRAL", "华中", "华中地区", "HUAZHONG", "CENTRAL", "CENTRALCHINA");
        }
        if (REGION_NORTH.equals(code))
        {
            return Arrays.asList("CNNORTH", "华北", "华北地区", "HUABEI", "NORTH", "NORTHCHINA");
        }
        if (REGION_SOUTH.equals(code))
        {
            return Arrays.asList("CNSOUTH", "华南", "华南地区", "HUANAN", "SOUTH", "SOUTHCHINA");
        }
        if (REGION_NORTHEAST.equals(code))
        {
            return Arrays.asList("CNNORTHEAST", "东北", "东北地区", "DONGBEI", "NORTHEAST");
        }
        if (REGION_SOUTHWEST.equals(code))
        {
            return Arrays.asList("CNSOUTHWEST", "西南", "西南地区", "XINAN", "SOUTHWEST");
        }
        if (REGION_NORTHWEST.equals(code))
        {
            return Arrays.asList("CNNORTHWEST", "西北", "西北地区", "XIBEI", "NORTHWEST");
        }
        if (REGION_HK_MO_TW.equals(code))
        {
            return Arrays.asList("CNHKMOTW", "港澳台", "港台澳", "HONGKONGMACAUTAIWAN");
        }
        return Arrays.asList(normalizeRegionToken(code), normalizeRegionToken(regionNameByCode(code)));
    }

    private String normalizeRegionToken(Object value)
    {
        String text = trimToEmpty(value);
        if (StringUtils.isEmpty(text))
        {
            return "";
        }
        return text
            .toUpperCase(Locale.ROOT)
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "");
    }

    private String canonicalRegionCode(Object rawCode)
    {
        String token = normalizeRegionToken(rawCode);
        if (StringUtils.isEmpty(token))
        {
            return "";
        }
        if (isRegionTokenMatch(token, REGION_NORTH))
        {
            return REGION_NORTH;
        }
        if (isRegionTokenMatch(token, REGION_EAST))
        {
            return REGION_EAST;
        }
        if (isRegionTokenMatch(token, REGION_CENTRAL))
        {
            return REGION_CENTRAL;
        }
        if (isRegionTokenMatch(token, REGION_SOUTH))
        {
            return REGION_SOUTH;
        }
        if (isRegionTokenMatch(token, REGION_NORTHEAST))
        {
            return REGION_NORTHEAST;
        }
        if (isRegionTokenMatch(token, REGION_SOUTHWEST))
        {
            return REGION_SOUTHWEST;
        }
        if (isRegionTokenMatch(token, REGION_NORTHWEST))
        {
            return REGION_NORTHWEST;
        }
        if (isRegionTokenMatch(token, REGION_HK_MO_TW))
        {
            return REGION_HK_MO_TW;
        }
        return "";
    }

    private boolean isRegionTokenMatch(String token, String regionCode)
    {
        if (StringUtils.isEmpty(token))
        {
            return false;
        }
        if (token.equals(normalizeRegionToken(regionCode)))
        {
            return true;
        }
        for (String alias : regionAliasTokens(regionCode))
        {
            if (token.equals(normalizeRegionToken(alias)))
            {
                return true;
            }
        }
        return false;
    }

    private String detectRegionCodeByAddress(String receiverAddress)
    {
        String province = normalizeProvinceName(extractProvinceToken(receiverAddress));
        if (StringUtils.isEmpty(province))
        {
            return "";
        }
        if (belongsToProvince(province, "北京", "天津", "河北", "山西", "内蒙古"))
        {
            return REGION_NORTH;
        }
        if (belongsToProvince(province, "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东"))
        {
            return REGION_EAST;
        }
        if (belongsToProvince(province, "河南", "湖北", "湖南"))
        {
            return REGION_CENTRAL;
        }
        if (belongsToProvince(province, "广东", "广西", "海南"))
        {
            return REGION_SOUTH;
        }
        if (belongsToProvince(province, "辽宁", "吉林", "黑龙江"))
        {
            return REGION_NORTHEAST;
        }
        if (belongsToProvince(province, "重庆", "四川", "贵州", "云南", "西藏"))
        {
            return REGION_SOUTHWEST;
        }
        if (belongsToProvince(province, "陕西", "甘肃", "青海", "宁夏", "新疆"))
        {
            return REGION_NORTHWEST;
        }
        if (belongsToProvince(province, "香港", "澳门", "台湾"))
        {
            return REGION_HK_MO_TW;
        }
        return "";
    }

    private String extractProvinceToken(String receiverAddress)
    {
        String text = trimToEmpty(receiverAddress)
            .replace("，", " ")
            .replace(",", " ")
            .replace("／", "/")
            .replace("\\", "/")
            .replaceAll("\\s+", " ")
            .trim();
        if (StringUtils.isEmpty(text))
        {
            return "";
        }
        if (text.contains("/"))
        {
            String[] slashParts = text.split("/");
            if (slashParts.length > 0 && !StringUtils.isEmpty(slashParts[0].trim()))
            {
                return slashParts[0].trim();
            }
        }
        if (text.contains(" "))
        {
            String[] parts = text.split(" ");
            if (parts.length > 0 && !StringUtils.isEmpty(parts[0].trim()))
            {
                return parts[0].trim();
            }
        }

        String compact = text.replace(" ", "");
        List<String> fullNameList = Arrays.asList(
            "北京市", "天津市", "河北省", "山西省", "内蒙古自治区",
            "上海市", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
            "河南省", "湖北省", "湖南省",
            "广东省", "广西壮族自治区", "海南省",
            "辽宁省", "吉林省", "黑龙江省",
            "重庆市", "四川省", "贵州省", "云南省", "西藏自治区",
            "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区",
            "香港特别行政区", "澳门特别行政区", "台湾省");
        for (String fullName : fullNameList)
        {
            if (compact.startsWith(fullName))
            {
                return fullName;
            }
        }
        return "";
    }

    private String normalizeProvinceName(String provinceToken)
    {
        String province = trimToEmpty(provinceToken);
        if (StringUtils.isEmpty(province))
        {
            return "";
        }
        return province
            .replace("特别行政区", "")
            .replace("自治区", "")
            .replace("维吾尔", "")
            .replace("壮族", "")
            .replace("回族", "")
            .replace("省", "")
            .replace("市", "")
            .trim();
    }

    private boolean belongsToProvince(String province, String... names)
    {
        if (StringUtils.isEmpty(province) || names == null)
        {
            return false;
        }
        for (String name : names)
        {
            if (province.equals(name))
            {
                return true;
            }
        }
        return false;
    }

    private String regionNameByCode(String regionCode)
    {
        String code = trimToEmpty(regionCode);
        if (REGION_EAST.equals(code))
        {
            return "华东";
        }
        if (REGION_CENTRAL.equals(code))
        {
            return "华中";
        }
        if (REGION_NORTH.equals(code))
        {
            return "华北";
        }
        if (REGION_SOUTH.equals(code))
        {
            return "华南";
        }
        if (REGION_NORTHEAST.equals(code))
        {
            return "东北";
        }
        if (REGION_SOUTHWEST.equals(code))
        {
            return "西南";
        }
        if (REGION_NORTHWEST.equals(code))
        {
            return "西北";
        }
        if (REGION_HK_MO_TW.equals(code))
        {
            return "港澳台";
        }
        return "未识别地区";
    }

    private boolean isMallAdminUser(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return hasRole(userId, ADMIN_ROLE_KEY) || hasRole(userId, SYS_ADMIN_ROLE_KEY);
    }

    private boolean hasRole(Long userId, String roleKey)
    {
        List<Long> userIds = agriMallMapper.selectUserIdsByRoleKey(roleKey);
        return userIds != null && userIds.contains(userId);
    }

    private void addFarmerNotice(Long farmerId, String noticeType, Long bizId, String title, String content, Date createTime)
    {
        if (farmerId == null)
        {
            return;
        }
        Map<String, Object> noticeMap = new HashMap<>();
        noticeMap.put("farmerId", farmerId);
        noticeMap.put("noticeType", noticeType);
        noticeMap.put("bizId", bizId);
        noticeMap.put("title", title);
        noticeMap.put("content", content);
        noticeMap.put("readFlag", "0");
        noticeMap.put("createTime", createTime == null ? new Date() : createTime);
        noticeMap.put("readTime", null);
        agriMallMapper.insertFarmerNotice(noticeMap);
    }

    private void addAdminNoticeForRole(String roleKey, String noticeType, Long bizId, String title, String content, Date createTime)
    {
        List<Long> adminIds = agriMallMapper.selectUserIdsByRoleKey(roleKey);
        if (adminIds == null || adminIds.isEmpty())
        {
            return;
        }
        for (Long adminId : adminIds)
        {
            addAdminNotice(adminId, noticeType, bizId, title, content, createTime);
        }
    }

    private void addAdminNotice(Long adminId, String noticeType, Long bizId, String title, String content, Date createTime)
    {
        if (adminId == null)
        {
            return;
        }
        Map<String, Object> noticeMap = new HashMap<>();
        noticeMap.put("adminId", adminId);
        noticeMap.put("noticeType", noticeType);
        noticeMap.put("bizId", bizId);
        noticeMap.put("title", title);
        noticeMap.put("content", content);
        noticeMap.put("readFlag", "0");
        noticeMap.put("createTime", createTime == null ? new Date() : createTime);
        noticeMap.put("readTime", null);
        agriMallMapper.insertAdminNotice(noticeMap);
    }

    private void appendVirtualTraceIfMissing(List<Map<String, Object>> traces, String node, String desc, Date time)
    {
        if (traces == null)
        {
            return;
        }
        boolean exists = traces.stream().anyMatch(item -> node.equalsIgnoreCase(toStr(item.get("statusNode"))));
        if (exists)
        {
            return;
        }
        Map<String, Object> virtualTrace = new HashMap<>();
        virtualTrace.put("traceId", null);
        virtualTrace.put("orderId", null);
        virtualTrace.put("statusNode", node);
        virtualTrace.put("statusDesc", desc);
        virtualTrace.put("traceTime", time == null ? new Date() : time);
        traces.add(virtualTrace);
    }

    private String channelName(String channel)
    {
        if ("2".equals(channel))
        {
            return "支付宝沙箱";
        }
        return "模拟支付";
    }

    private Date afterMinutes(Date base, int minutes)
    {
        Date actualBase = base == null ? new Date() : base;
        return new Date(actualBase.getTime() + minutes * 60L * 1000L);
    }

    private int normalizeStar(Object value)
    {
        int star = toInt(value, 5);
        if (star < 1)
        {
            return 1;
        }
        if (star > 5)
        {
            return 5;
        }
        return star;
    }

    private String normalizeProductStatus(String status, String defaultValue)
    {
        if ("0".equals(status) || "1".equals(status) || "2".equals(status) || "3".equals(status))
        {
            return status;
        }
        return StringUtils.isEmpty(defaultValue) ? "1" : defaultValue;
    }

    private boolean isValidPayStatus(String status)
    {
        return "0".equals(status) || "1".equals(status) || "2".equals(status);
    }

    private boolean isValidOrderStatus(String status)
    {
        return "0".equals(status)
            || "1".equals(status)
            || "2".equals(status)
            || "3".equals(status)
            || "4".equals(status)
            || "5".equals(status)
            || "6".equals(status)
            || "7".equals(status);
    }

    private String traceNodeByStatus(String status)
    {
        if ("1".equals(status))
        {
            return "ACCEPTED";
        }
        if ("2".equals(status))
        {
            return "SHIPPED";
        }
        if ("3".equals(status))
        {
            return "OUT_FOR_DELIVERY";
        }
        if ("4".equals(status))
        {
            return "RECEIVED";
        }
        if ("5".equals(status))
        {
            return "CANCELLED";
        }
        if ("6".equals(status))
        {
            return "REFUND_APPLY";
        }
        if ("7".equals(status))
        {
            return "REFUND_APPROVED";
        }
        return "ORDER_CREATED";
    }

    private Map<String, Object> defaultMap(Map<String, Object> map)
    {
        return map == null ? new HashMap<>() : map;
    }

    private String generateOrderNo()
    {
        return "AG" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private String generatePayNo()
    {
        return "PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private String trimToEmpty(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String toStr(Object value)
    {
        return value == null ? "" : String.valueOf(value);
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
        String text = String.valueOf(value);
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        return Long.valueOf(text);
    }

    private int toInt(Object value, int defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value);
        if (StringUtils.isEmpty(text))
        {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        if (value instanceof Number)
        {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        String text = String.valueOf(value);
        if (StringUtils.isEmpty(text))
        {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text);
    }

    private Date toDate(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Date)
        {
            return (Date) value;
        }
        return null;
    }
}


