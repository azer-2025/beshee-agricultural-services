package com.bishe.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.config.PaymentSandboxProperties;
import com.bishe.system.mapper.AgriMallMapper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgriMallPayService
{
    private static final Logger log = LoggerFactory.getLogger(AgriMallPayService.class);
    private static final String CHANNEL_MOCK = "1";
    private static final String CHANNEL_ALIPAY = "2";
    private static final String PAID_STATUS = "1";
    private static final String ORDER_PENDING_SHIP = "1";
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";
    private static final String CHARSET = "utf-8";
    private static final String FORMAT = "JSON";
    private static final String SIGN_TYPE = "RSA2";
    private static final String VERSION = "1.0";

    @Autowired
    private AgriMallMapper agriMallMapper;

    @Autowired
    private PaymentSandboxProperties paymentSandboxProperties;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payOrder(Long buyerId, Long orderId, String channel)
    {
        if (orderId == null)
        {
            throw new ServiceException("订单ID不能为空");
        }
        Map<String, Object> order = agriMallMapper.selectOrderByBuyer(orderId, buyerId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(trimToEmpty(order.get("pay_status"))))
        {
            throw new ServiceException("订单已支付，请勿重复支付");
        }
        if (!"0".equals(trimToEmpty(order.get("order_status"))))
        {
            throw new ServiceException("当前订单状态不允许支付");
        }

        String realChannel = normalizeChannel(channel);
        if (StringUtils.isEmpty(realChannel))
        {
            realChannel = defaultChannel(order);
        }
        if (StringUtils.isEmpty(realChannel))
        {
            realChannel = CHANNEL_MOCK;
        }
        if (CHANNEL_ALIPAY.equals(realChannel))
        {
            if (!paymentSandboxProperties.isEnabled())
            {
                throw new ServiceException("支付宝沙箱未启用");
            }
            if (!paymentSandboxProperties.isAutoSuccess())
            {
                return buildAlipayPayResult(order);
            }
        }
        return markPaidByBuyerOrder(order, buyerId, realChannel, generatePayNo(), new Date(), "模拟支付成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params)
    {
        if (!isRealAlipayEnabled() || params == null || params.isEmpty())
        {
            return "failure";
        }
        if (!verifyAlipaySign(params))
        {
            return "failure";
        }
        if (!StringUtils.equals(trimToEmpty(paymentSandboxProperties.getAlipay().getAppId()), trimToEmpty(params.get("app_id"))))
        {
            return "failure";
        }
        String tradeStatus = trimToEmpty(params.get("trade_status"));
        if (!isTradePaid(tradeStatus))
        {
            return "success";
        }
        try
        {
            handlePaidCallback(params, "支付宝沙箱支付成功");
            return "success";
        }
        catch (Exception ex)
        {
            return "failure";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleAlipayReturn(Map<String, String> params)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("verified", false);
        result.put("success", false);
        result.put("orderNo", trimToEmpty(params == null ? null : params.get("out_trade_no")));
        result.put("tradeStatus", trimToEmpty(params == null ? null : params.get("trade_status")));
        if (!isRealAlipayEnabled() || params == null || params.isEmpty())
        {
            result.put("message", "沙箱支付未启用或回调参数为空");
            return result;
        }
        if (!verifyAlipaySign(params))
        {
            result.put("message", "回调验签失败");
            return result;
        }
        if (!StringUtils.equals(trimToEmpty(paymentSandboxProperties.getAlipay().getAppId()), trimToEmpty(params.get("app_id"))))
        {
            result.put("message", "应用ID不匹配");
            return result;
        }
        result.put("verified", true);
        if (!isTradePaid(trimToEmpty(params.get("trade_status"))))
        {
            result.put("message", "交易尚未支付完成");
            return result;
        }
        handlePaidCallback(params, "支付宝沙箱支付成功");
        result.put("success", true);
        result.put("message", "支付成功");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean syncPendingAlipayOrders(List<Map<String, Object>> orders)
    {
        if (!isRealAlipayEnabled() || orders == null || orders.isEmpty())
        {
            return false;
        }
        boolean changed = false;
        for (Map<String, Object> row : orders)
        {
            if (row == null)
            {
                continue;
            }
            if (!"0".equals(trimToEmpty(row.get("payStatus"))))
            {
                continue;
            }
            if (!CHANNEL_ALIPAY.equals(trimToEmpty(row.get("paymentChannel"))))
            {
                continue;
            }
            String orderNo = trimToEmpty(row.get("orderNo"));
            if (StringUtils.isEmpty(orderNo))
            {
                continue;
            }
            Map<String, String> queryResult = queryTradeByOrderNo(orderNo);
            if (queryResult == null || !isTradePaid(trimToEmpty(queryResult.get("trade_status"))))
            {
                continue;
            }
            try
            {
                handlePaidCallback(queryResult, "Alipay sandbox payment confirmed");
                changed = true;
            }
            catch (Exception ex)
            {
                log.warn("sync pending alipay order failed, orderNo={}", orderNo, ex);
            }
        }
        return changed;
    }

    private Map<String, Object> buildAlipayPayResult(Map<String, Object> order)
    {
        validateAlipayConfig();
        String orderNo = trimToEmpty(order.get("order_no"));
        Long orderId = toLong(order.get("order_id"));
        BigDecimal payAmount = toAmount(order.get("pay_amount"));
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return markPaidByOrderNo(orderNo, CHANNEL_ALIPAY, generatePayNo(), new Date(), "零元订单自动支付");
        }

        PaymentSandboxProperties.Alipay alipay = paymentSandboxProperties.getAlipay();
        Map<String, Object> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", orderNo);
        bizContent.put("total_amount", payAmount.toPlainString());
        bizContent.put("subject", "助农商城订单-" + orderNo);
        bizContent.put("product_code", trimToEmpty(alipay.getProductCode()));
        bizContent.put("timeout_express", "30m");

        Map<String, String> params = new TreeMap<>();
        params.put("app_id", trimToEmpty(alipay.getAppId()));
        params.put("method", trimToEmpty(alipay.getMethod()));
        params.put("format", FORMAT);
        params.put("charset", CHARSET);
        params.put("sign_type", SIGN_TYPE);
        params.put("timestamp", formatDateTime(new Date()));
        params.put("version", VERSION);
        params.put("notify_url", trimToEmpty(alipay.getNotifyUrl()));
        params.put("return_url", trimToEmpty(alipay.getReturnUrl()));
        params.put("biz_content", JSON.toJSONString(bizContent));

        String signContent = buildSignContent(params, true);
        String sign = rsaSign(signContent, trimToEmpty(alipay.getMerchantPrivateKey()));
        String query = toQueryString(params) + "&sign=" + urlEncode(sign);
        String payUrl = trimToEmpty(alipay.getGatewayUrl());
        if (payUrl.contains("?"))
        {
            payUrl = payUrl + "&" + query;
        }
        else
        {
            payUrl = payUrl + "?" + query;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", orderNo);
        result.put("channel", CHANNEL_ALIPAY);
        result.put("channelName", "支付宝沙箱");
        result.put("payAmount", payAmount);
        result.put("payMode", "REDIRECT");
        result.put("payUrl", payUrl);
        result.put("autoSuccess", false);
        return result;
    }

    private void handlePaidCallback(Map<String, String> params, String traceDesc)
    {
        String orderNo = trimToEmpty(params.get("out_trade_no"));
        if (StringUtils.isEmpty(orderNo))
        {
            throw new ServiceException("缺少交易订单号（out_trade_no）");
        }
        Map<String, Object> order = agriMallMapper.selectOrderByNo(orderNo);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        checkPaidAmount(order, params.get("total_amount"));
        if ("1".equals(trimToEmpty(order.get("pay_status"))))
        {
            return;
        }
        String orderStatus = trimToEmpty(order.get("order_status"));
        if ("5".equals(orderStatus) || "7".equals(orderStatus))
        {
            return;
        }

        Date payTime = parseAlipayTime(params.get("gmt_payment"));
        String tradeNo = trimToEmpty(params.get("trade_no"));
        if (StringUtils.isEmpty(tradeNo))
        {
            tradeNo = generatePayNo();
        }
        Map<String, Object> payMap = new HashMap<>();
        payMap.put("orderNo", orderNo);
        payMap.put("payStatus", PAID_STATUS);
        payMap.put("orderStatus", ORDER_PENDING_SHIP);
        payMap.put("paymentChannel", CHANNEL_ALIPAY);
        payMap.put("payVoucherNo", tradeNo);
        payMap.put("payTime", payTime);
        payMap.put("updateTime", new Date());
        int rows = agriMallMapper.updateOrderPayByOrderNo(payMap);
        if (rows <= 0)
        {
            return;
        }

        Long orderId = toLong(order.get("order_id"));
        Map<String, Object> voucherMap = new HashMap<>();
        voucherMap.put("orderId", orderId);
        voucherMap.put("payNo", tradeNo);
        voucherMap.put("channel", CHANNEL_ALIPAY);
        voucherMap.put("voucherImg", "");
        voucherMap.put("payAmount", toAmount(order.get("pay_amount")));
        voucherMap.put("payTime", payTime);
        voucherMap.put("status", PAID_STATUS);
        agriMallMapper.insertPaymentVoucher(voucherMap);

        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put("orderId", orderId);
        traceMap.put("statusNode", "PAID");
        traceMap.put("statusDesc", traceDesc);
        traceMap.put("traceTime", payTime);
        agriMallMapper.insertLogisticsTrace(traceMap);
    }

    private Map<String, Object> markPaidByBuyerOrder(Map<String, Object> order, Long buyerId, String channel, String payNo, Date payTime, String traceDesc)
    {
        Long orderId = toLong(order.get("order_id"));
        Map<String, Object> payMap = new HashMap<>();
        payMap.put("orderId", orderId);
        payMap.put("buyerId", buyerId);
        payMap.put("payStatus", PAID_STATUS);
        payMap.put("orderStatus", ORDER_PENDING_SHIP);
        payMap.put("paymentChannel", channel);
        payMap.put("payVoucherNo", payNo);
        payMap.put("payTime", payTime);
        payMap.put("updateTime", payTime);
        int rows = agriMallMapper.updateOrderPay(payMap);
        if (rows <= 0)
        {
            throw new ServiceException("订单支付状态更新失败");
        }

        Map<String, Object> voucherMap = new HashMap<>();
        voucherMap.put("orderId", orderId);
        voucherMap.put("payNo", payNo);
        voucherMap.put("channel", channel);
        voucherMap.put("voucherImg", "");
        voucherMap.put("payAmount", toAmount(order.get("pay_amount")));
        voucherMap.put("payTime", payTime);
        voucherMap.put("status", PAID_STATUS);
        agriMallMapper.insertPaymentVoucher(voucherMap);

        Map<String, Object> traceMap = new HashMap<>();
        traceMap.put("orderId", orderId);
        traceMap.put("statusNode", "PAID");
        traceMap.put("statusDesc", traceDesc);
        traceMap.put("traceTime", payTime);
        agriMallMapper.insertLogisticsTrace(traceMap);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", trimToEmpty(order.get("order_no")));
        result.put("payNo", payNo);
        result.put("channel", channel);
        result.put("channelName", channelName(channel));
        result.put("payAmount", toAmount(order.get("pay_amount")));
        result.put("payTime", payTime);
        result.put("payMode", "MOCK");
        return result;
    }

    private Map<String, Object> markPaidByOrderNo(String orderNo, String channel, String payNo, Date payTime, String traceDesc)
    {
        Map<String, Object> order = agriMallMapper.selectOrderByNo(orderNo);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        Map<String, String> callbackParams = new HashMap<>();
        callbackParams.put("out_trade_no", orderNo);
        callbackParams.put("trade_no", payNo);
        callbackParams.put("trade_status", TRADE_SUCCESS);
        callbackParams.put("total_amount", toAmount(order.get("pay_amount")).toPlainString());
        callbackParams.put("gmt_payment", formatDateTime(payTime));
        handlePaidCallback(callbackParams, traceDesc);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", toLong(order.get("order_id")));
        result.put("orderNo", orderNo);
        result.put("payNo", payNo);
        result.put("channel", channel);
        result.put("channelName", channelName(channel));
        result.put("payAmount", toAmount(order.get("pay_amount")));
        result.put("payTime", payTime);
        result.put("payMode", "MOCK");
        return result;
    }

    private boolean verifyAlipaySign(Map<String, String> params)
    {
        try
        {
            String sign = trimToEmpty(params.get("sign"));
            if (StringUtils.isEmpty(sign))
            {
                return false;
            }
            String signContent = buildSignContent(params, false);
            PublicKey publicKey = loadPublicKey(trimToEmpty(paymentSandboxProperties.getAlipay().getAlipayPublicKey()));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(signContent.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(sign));
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private String buildSignContent(Map<String, String> source, boolean includeSignType)
    {
        return source.entrySet().stream()
            .filter(e -> StringUtils.isNotEmpty(trimToEmpty(e.getKey())))
            .filter(e -> StringUtils.isNotEmpty(trimToEmpty(e.getValue())))
            .filter(e -> !"sign".equalsIgnoreCase(trimToEmpty(e.getKey())))
            .filter(e -> includeSignType || !"sign_type".equalsIgnoreCase(trimToEmpty(e.getKey())))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
    }

    private String toQueryString(Map<String, String> params)
    {
        return params.entrySet().stream()
            .filter(e -> StringUtils.isNotEmpty(trimToEmpty(e.getValue())))
            .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
            .collect(Collectors.joining("&"));
    }

    private Map<String, String> queryTradeByOrderNo(String orderNo)
    {
        try
        {
            validateAlipayConfig();
            PaymentSandboxProperties.Alipay alipay = paymentSandboxProperties.getAlipay();
            Map<String, Object> bizContent = new LinkedHashMap<>();
            bizContent.put("out_trade_no", orderNo);

            Map<String, String> params = new TreeMap<>();
            params.put("app_id", trimToEmpty(alipay.getAppId()));
            params.put("method", "alipay.trade.query");
            params.put("format", FORMAT);
            params.put("charset", CHARSET);
            params.put("sign_type", SIGN_TYPE);
            params.put("timestamp", formatDateTime(new Date()));
            params.put("version", VERSION);
            params.put("biz_content", JSON.toJSONString(bizContent));
            String sign = rsaSign(buildSignContent(params, true), trimToEmpty(alipay.getMerchantPrivateKey()));
            params.put("sign", sign);

            String responseText = doGatewayPost(trimToEmpty(alipay.getGatewayUrl()), toQueryString(params));
            if (StringUtils.isEmpty(responseText))
            {
                return null;
            }
            com.alibaba.fastjson2.JSONObject root = JSON.parseObject(responseText);
            com.alibaba.fastjson2.JSONObject queryResp = root.getJSONObject("alipay_trade_query_response");
            if (queryResp == null || !"10000".equals(trimToEmpty(queryResp.getString("code"))))
            {
                return null;
            }
            Map<String, String> result = new HashMap<>();
            result.put("out_trade_no", trimToEmpty(queryResp.getString("out_trade_no")));
            result.put("trade_no", trimToEmpty(queryResp.getString("trade_no")));
            result.put("trade_status", trimToEmpty(queryResp.getString("trade_status")));
            result.put("total_amount", trimToEmpty(queryResp.getString("total_amount")));
            String sendPayDate = trimToEmpty(queryResp.getString("send_pay_date"));
            result.put("gmt_payment", StringUtils.isEmpty(sendPayDate) ? formatDateTime(new Date()) : sendPayDate);
            return result;
        }
        catch (Exception ex)
        {
            log.warn("query alipay trade failed, orderNo={}", orderNo, ex);
            return null;
        }
    }

    private String doGatewayPost(String gatewayUrl, String body) throws Exception
    {
        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) new URL(gatewayUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream outputStream = conn.getOutputStream())
            {
                outputStream.write(bytes);
                outputStream.flush();
            }
            int code = conn.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null)
            {
                return "";
            }
            try (InputStream inputStream = stream)
            {
                return readAllBytes(inputStream);
            }
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    private String readAllBytes(InputStream inputStream) throws Exception
    {
        byte[] buf = new byte[4096];
        int len;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            while ((len = inputStream.read(buf)) != -1)
            {
                out.write(buf, 0, len);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private String rsaSign(String content, String privateKeyText)
    {
        try
        {
            PrivateKey privateKey = loadPrivateKey(privateKeyText);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch (Exception ex)
        {
            throw new ServiceException("支付宝签名失败");
        }
    }

    private PrivateKey loadPrivateKey(String keyText) throws Exception
    {
        String normalized = normalizeKey(keyText);
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String keyText) throws Exception
    {
        String normalized = normalizeKey(keyText);
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private String normalizeKey(String keyText)
    {
        return trimToEmpty(keyText)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
    }

    private boolean isTradePaid(String tradeStatus)
    {
        return TRADE_SUCCESS.equals(tradeStatus) || TRADE_FINISHED.equals(tradeStatus);
    }

    private void checkPaidAmount(Map<String, Object> order, String paidAmountText)
    {
        if (StringUtils.isEmpty(trimToEmpty(paidAmountText)))
        {
            return;
        }
        BigDecimal orderAmount = toAmount(order.get("pay_amount"));
        BigDecimal paidAmount = toAmount(paidAmountText);
        if (orderAmount.compareTo(paidAmount) != 0)
        {
            throw new ServiceException("支付金额校验不一致");
        }
    }

    private boolean isRealAlipayEnabled()
    {
        return paymentSandboxProperties.isEnabled() && !paymentSandboxProperties.isAutoSuccess();
    }

    private void validateAlipayConfig()
    {
        PaymentSandboxProperties.Alipay alipay = paymentSandboxProperties.getAlipay();
        if (alipay == null
            || StringUtils.isEmpty(trimToEmpty(alipay.getAppId()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getMerchantPrivateKey()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getAlipayPublicKey()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getGatewayUrl()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getNotifyUrl()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getReturnUrl()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getMethod()))
            || StringUtils.isEmpty(trimToEmpty(alipay.getProductCode())))
        {
            throw new ServiceException("支付宝沙箱配置不完整");
        }
    }

    private String defaultChannel(Map<String, Object> order)
    {
        return normalizeChannel(trimToEmpty(order.get("payment_channel")));
    }

    private String normalizeChannel(String channel)
    {
        String value = trimToEmpty(channel);
        if (StringUtils.isEmpty(value) || "null".equalsIgnoreCase(value) || "undefined".equalsIgnoreCase(value))
        {
            return "";
        }
        return value;
    }

    private String channelName(String channel)
    {
        if (CHANNEL_ALIPAY.equals(channel))
        {
            return "支付宝沙箱";
        }
        return "模拟支付";
    }

    private String generatePayNo()
    {
        String timePart = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PAY" + timePart + randomPart;
    }

    private String trimToEmpty(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
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

    private BigDecimal toAmount(Object value)
    {
        if (value == null || StringUtils.isEmpty(String.valueOf(value)))
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
    }

    private String formatDateTime(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(date == null ? new Date() : date);
    }

    private Date parseAlipayTime(String value)
    {
        if (StringUtils.isEmpty(trimToEmpty(value)))
        {
            return new Date();
        }
        try
        {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(value.trim());
        }
        catch (ParseException ex)
        {
            return new Date();
        }
    }

    private String urlEncode(String value)
    {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
