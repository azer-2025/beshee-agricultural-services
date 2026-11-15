package com.bishe.system.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bishe.common.exception.ServiceException;
import com.bishe.common.utils.StringUtils;
import com.bishe.system.config.AgriAiProperties;
import com.bishe.system.mapper.AgriMallMapper;
import com.bishe.system.service.IAgriAiService;

@Service
public class AgriAiServiceImpl implements IAgriAiService
{
    private static final Logger log = LoggerFactory.getLogger(AgriAiServiceImpl.class);
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    @Autowired
    private AgriMallMapper agriMallMapper;

    @Autowired
    private AgriAiProperties agriAiProperties;

    @Override
    public Map<String, Object> chat(Long buyerId, Map<String, Object> form)
    {
        String message = trimToEmpty(form == null ? null : form.get("message"));
        if (StringUtils.isEmpty(message))
        {
            throw new ServiceException("Message is required");
        }

        List<Map<String, String>> history = normalizeHistory(form == null ? null : form.get("history"));
        Map<String, Object> context = loadBuyerContext(buyerId);
        String fallbackReply = buildFallbackReply(message, context);

        if (!agriAiProperties.isEnabled() || StringUtils.isEmpty(agriAiProperties.getApiKey()))
        {
            safePersistConversation(buyerId, message, fallbackReply, "fallback");
            return buildResult(fallbackReply, "fallback");
        }

        try
        {
            String reply = requestRemoteReply(message, history, context);
            if (StringUtils.isEmpty(reply))
            {
                safePersistConversation(buyerId, message, fallbackReply, "fallback");
                return buildResult(fallbackReply, "fallback");
            }
            safePersistConversation(buyerId, message, reply, "remote");
            return buildResult(reply, "remote");
        }
        catch (Exception e)
        {
            log.warn("Agri AI remote call failed for buyerId={}: {}", buyerId, e.getMessage());
            safePersistConversation(buyerId, message, fallbackReply, "fallback");
            return buildResult(fallbackReply, "fallback");
        }
    }

    @Override
    public List<Map<String, Object>> listHistory(Long buyerId, Integer limit)
    {
        int size = limit == null || limit <= 0 ? 20 : Math.min(limit, 50);
        List<Map<String, Object>> rows;
        try
        {
            rows = agriMallMapper.selectAiChatLogList(buyerId, size);
        }
        catch (Exception e)
        {
            log.warn("Agri AI history load failed for buyerId={}: {}", buyerId, e.getMessage());
            return Collections.emptyList();
        }
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        Collections.reverse(rows);
        return rows;
    }

    @Override
    public int clearHistory(Long buyerId)
    {
        try
        {
            return agriMallMapper.deleteAiChatLogByBuyer(buyerId);
        }
        catch (Exception e)
        {
            log.warn("Agri AI history clear failed for buyerId={}: {}", buyerId, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> listAdminHistory(Map<String, Object> query)
    {
        return agriMallMapper.selectAdminAiChatLogList(query == null ? new HashMap<String, Object>() : query);
    }

    private Map<String, Object> buildResult(String reply, String mode)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("mode", mode);
        result.put("provider", agriAiProperties.getProvider());
        result.put("model", agriAiProperties.getModel());
        return result;
    }

    private void persistConversation(Long buyerId, String userMessage, String assistantReply, String mode)
    {
        Date now = new Date();
        insertLog(buyerId, "user", userMessage, mode, now);
        insertLog(buyerId, "assistant", assistantReply, mode, now);
    }

    private void safePersistConversation(Long buyerId, String userMessage, String assistantReply, String mode)
    {
        try
        {
            persistConversation(buyerId, userMessage, assistantReply, mode);
        }
        catch (Exception e)
        {
            log.warn("Agri AI history persist failed for buyerId={}: {}", buyerId, e.getMessage());
        }
    }

    private void insertLog(Long buyerId, String role, String content, String mode, Date createTime)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("buyerId", buyerId);
        data.put("role", role);
        data.put("content", truncate(content, 2000));
        data.put("replyMode", mode);
        data.put("provider", agriAiProperties.getProvider());
        data.put("modelName", agriAiProperties.getModel());
        data.put("createTime", createTime == null ? new Date() : createTime);
        agriMallMapper.insertAiChatLog(data);
    }

    private String requestRemoteReply(String message, List<Map<String, String>> history, Map<String, Object> context)
    {
        JSONObject payload = new JSONObject();
        payload.put("model", agriAiProperties.getModel());
        payload.put("temperature", agriAiProperties.getTemperature() == null ? 0.3D : agriAiProperties.getTemperature());
        payload.put("stream", false);

        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", buildSystemPrompt()));
        messages.add(buildMessage("system", buildContextPrompt(context)));
        for (Map<String, String> item : history)
        {
            messages.add(buildMessage(item.get("role"), item.get("content")));
        }
        messages.add(buildMessage("user", truncate(message, 600)));
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(agriAiProperties.getApiKey());
        HttpEntity<String> entity = new HttpEntity<>(payload.toJSONString(), headers);

        ResponseEntity<String> response = buildRestTemplate().postForEntity(buildChatUrl(), entity, String.class);
        JSONObject root = JSON.parseObject(response.getBody());
        JSONArray choices = root == null ? null : root.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            return "";
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject messageNode = first == null ? null : first.getJSONObject("message");
        return messageNode == null ? "" : trimToEmpty(messageNode.getString("content"));
    }

    private JSONObject buildMessage(String role, String content)
    {
        JSONObject item = new JSONObject();
        item.put("role", StringUtils.isEmpty(role) ? "user" : role);
        item.put("content", StringUtils.isEmpty(content) ? "" : content);
        return item;
    }

    private RestTemplate buildRestTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(agriAiProperties.getConnectTimeoutMs() == null ? 10000 : agriAiProperties.getConnectTimeoutMs());
        factory.setReadTimeout(agriAiProperties.getReadTimeoutMs() == null ? 20000 : agriAiProperties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    private String buildChatUrl()
    {
        String baseUrl = trimToEmpty(agriAiProperties.getBaseUrl());
        String chatPath = trimToEmpty(agriAiProperties.getChatPath());
        if (StringUtils.isEmpty(baseUrl))
        {
            throw new ServiceException("AI baseUrl is not configured");
        }
        if (StringUtils.isEmpty(chatPath))
        {
            return baseUrl;
        }
        boolean baseEndsWithSlash = baseUrl.endsWith("/");
        boolean pathStartsWithSlash = chatPath.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash)
        {
            return baseUrl.substring(0, baseUrl.length() - 1) + chatPath;
        }
        if (!baseEndsWithSlash && !pathStartsWithSlash)
        {
            return baseUrl + "/" + chatPath;
        }
        return baseUrl + chatPath;
    }

    private String buildSystemPrompt()
    {
        return "你是助农果品销售系统的AI客服。请用简洁、自然的中文回答用户问题。"
            + "你只能根据提供的商城上下文回答，不要编造订单、价格、物流或售后结果。"
            + "如果信息不足，请明确说明并引导用户去订单跟踪、购物车、评价互动或人工咨询页面。";
    }

    private String buildContextPrompt(Map<String, Object> context)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是当前登录买家的真实上下文，请优先基于这些信息回答：\n");
        sb.append("用户昵称：").append(context.get("nickName")).append('\n');
        sb.append("购物车商品数：").append(context.get("cartCount")).append('\n');
        sb.append("待评价订单数：").append(context.get("pendingReviewCount")).append('\n');
        sb.append("默认收货地址：").append(context.get("defaultAddress")).append('\n');
        sb.append("最近订单：\n").append(context.get("recentOrders")).append('\n');
        sb.append("最近人工咨询：\n").append(context.get("recentConsults")).append('\n');
        sb.append("推荐商品：\n").append(context.get("recommendedProducts"));
        return sb.toString();
    }

    private Map<String, Object> loadBuyerContext(Long buyerId)
    {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("nickName", trimToEmpty(selectBuyerProfile(buyerId).get("nickName")));

        List<Map<String, Object>> cartList = agriMallMapper.selectCartList(buyerId);
        List<Map<String, Object>> reviewableItems = agriMallMapper.selectReviewableItems(buyerId);
        List<Map<String, Object>> addresses = agriMallMapper.selectBuyerAddressList(buyerId);

        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("buyerId", buyerId);
        List<Map<String, Object>> orders = agriMallMapper.selectBuyerOrderList(orderParams);

        Map<String, Object> disputeParams = new HashMap<>();
        disputeParams.put("buyerId", buyerId);
        List<Map<String, Object>> consults = agriMallMapper.selectDisputeList(disputeParams);

        List<Map<String, Object>> products = agriMallMapper.selectProductList(Collections.<String, Object>emptyMap());

        context.put("cartCount", cartList == null ? 0 : cartList.size());
        context.put("pendingReviewCount", reviewableItems == null ? 0 : reviewableItems.size());
        context.put("defaultAddress", summarizeDefaultAddress(addresses));
        context.put("recentOrders", summarizeOrders(orders));
        context.put("recentConsults", summarizeConsults(consults));
        context.put("recommendedProducts", summarizeProducts(products));
        return context;
    }

    private Map<String, Object> selectBuyerProfile(Long buyerId)
    {
        List<Map<String, Object>> addresses = agriMallMapper.selectBuyerAddressList(buyerId);
        Map<String, Object> profile = new HashMap<>();
        if (addresses != null && !addresses.isEmpty())
        {
            Map<String, Object> first = addresses.get(0);
            profile.put("nickName", trimToEmpty(first.get("userName")));
        }
        return profile;
    }

    private String summarizeDefaultAddress(List<Map<String, Object>> addresses)
    {
        if (addresses == null || addresses.isEmpty())
        {
            return "暂无默认地址";
        }
        for (Map<String, Object> item : addresses)
        {
            if ("1".equals(trimToEmpty(item.get("defaultFlag"))))
            {
                return summarizeAddressItem(item);
            }
        }
        return summarizeAddressItem(addresses.get(0));
    }

    private String summarizeAddressItem(Map<String, Object> item)
    {
        if (item == null)
        {
            return "暂无默认地址";
        }
        return trimToEmpty(item.get("receiverName")) + " " + trimToEmpty(item.get("receiverPhone")) + " " + trimToEmpty(item.get("receiverAddress"));
    }

    private String summarizeOrders(List<Map<String, Object>> orders)
    {
        if (orders == null || orders.isEmpty())
        {
            return "暂无订单";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(4, orders.size());
        for (int i = 0; i < limit; i++)
        {
            Map<String, Object> item = orders.get(i);
            sb.append(i + 1)
                .append(". 订单")
                .append(trimToEmpty(item.get("orderNo")))
                .append("，状态")
                .append(orderStatusText(item.get("orderStatus")))
                .append("，支付")
                .append(payStatusText(item.get("payStatus")))
                .append("，金额￥")
                .append(toMoney(item.get("payAmount")))
                .append("，下单时间")
                .append(formatTime(item.get("createTime")))
                .append('\n');
        }
        return sb.toString().trim();
    }

    private String summarizeConsults(List<Map<String, Object>> consults)
    {
        if (consults == null || consults.isEmpty())
        {
            return "暂无人工咨询";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, consults.size());
        for (int i = 0; i < limit; i++)
        {
            Map<String, Object> item = consults.get(i);
            sb.append(i + 1)
                .append(". ")
                .append(trimToEmpty(item.get("title")))
                .append("，状态")
                .append(consultStatusText(item.get("status")))
                .append("，回复")
                .append(StringUtils.isEmpty(trimToEmpty(item.get("handleResult"))) ? "暂无" : trimToEmpty(item.get("handleResult")))
                .append('\n');
        }
        return sb.toString().trim();
    }

    private String summarizeProducts(List<Map<String, Object>> products)
    {
        if (products == null || products.isEmpty())
        {
            return "暂无推荐商品";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(4, products.size());
        for (int i = 0; i < limit; i++)
        {
            Map<String, Object> item = products.get(i);
            sb.append(i + 1)
                .append(". ")
                .append(trimToEmpty(item.get("productName")))
                .append("，价格￥")
                .append(toMoney(item.get("price")))
                .append("，库存")
                .append(toInt(item.get("stock"), 0))
                .append("，农场")
                .append(trimToEmpty(item.get("farmName")))
                .append('\n');
        }
        return sb.toString().trim();
    }

    private String buildFallbackReply(String message, Map<String, Object> context)
    {
        String text = trimToEmpty(message).toLowerCase(Locale.ROOT);
        if (text.contains("退款") || text.contains("售后") || text.contains("退货"))
        {
            return "你可以先进入订单跟踪页发起退款申请。"
                + "如果订单已经发货，请在申请时写清退款原因。"
                + "当前你的订单上下文显示：" + context.get("recentOrders");
        }
        if (text.contains("物流") || text.contains("发货") || text.contains("订单") || text.contains("进度"))
        {
            return "这是你最近的订单摘要：\n" + context.get("recentOrders") + "\n如需更详细节点，请进入订单跟踪页查看物流轨迹。";
        }
        if (text.contains("评价") || text.contains("评论"))
        {
            return "你当前待评价订单数为 " + context.get("pendingReviewCount") + "。可以直接进入评价互动页完成评分和文字评价。";
        }
        if (text.contains("购物车") || text.contains("下单") || text.contains("结算"))
        {
            return "你购物车当前有 " + context.get("cartCount") + " 件商品。建议先确认默认地址“" + context.get("defaultAddress") + "”，再去购物车页统一结算。";
        }
        if (text.contains("推荐") || text.contains("商品") || text.contains("买什么"))
        {
            return "可以先看看这些推荐商品：\n" + context.get("recommendedProducts");
        }
        if (text.contains("人工") || text.contains("客服") || text.contains("咨询"))
        {
            return "如果需要转人工，你可以返回互动咨询页提交问题。当前最近的人工咨询记录为：\n" + context.get("recentConsults");
        }
        return "我可以协助你处理订单进度、退款售后、商品推荐、购物车结算和评价问题。"
            + "如果你想更快得到准确答复，可以直接告诉我是订单、退款、评价还是商品推荐。";
    }

    private List<Map<String, String>> normalizeHistory(Object raw)
    {
        if (!(raw instanceof List))
        {
            return Collections.emptyList();
        }
        List<?> source = (List<?>) raw;
        List<Map<String, String>> result = new ArrayList<>();
        int start = Math.max(0, source.size() - Math.max(0, agriAiProperties.getMaxHistory() == null ? 8 : agriAiProperties.getMaxHistory()));
        for (int i = start; i < source.size(); i++)
        {
            Object item = source.get(i);
            if (!(item instanceof Map))
            {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) item;
            String role = trimToEmpty(row.get("role"));
            String content = trimToEmpty(row.get("content"));
            if (StringUtils.isEmpty(content))
            {
                content = trimToEmpty(row.get("text"));
            }
            if (!"assistant".equals(role) && !"user".equals(role))
            {
                continue;
            }
            if (StringUtils.isEmpty(content))
            {
                continue;
            }
            Map<String, String> normalized = new HashMap<>();
            normalized.put("role", role);
            normalized.put("content", truncate(content, 400));
            result.add(normalized);
        }
        return result;
    }

    private String orderStatusText(Object value)
    {
        String code = trimToEmpty(value);
        if ("0".equals(code))
        {
            return "待接单";
        }
        if ("1".equals(code))
        {
            return "待发货";
        }
        if ("2".equals(code))
        {
            return "运输中";
        }
        if ("3".equals(code))
        {
            return "待收货";
        }
        if ("4".equals(code))
        {
            return "已完成";
        }
        if ("5".equals(code))
        {
            return "已取消";
        }
        if ("6".equals(code))
        {
            return "退款处理中";
        }
        if ("7".equals(code))
        {
            return "已退款";
        }
        return "未知状态";
    }

    private String payStatusText(Object value)
    {
        String code = trimToEmpty(value);
        if ("0".equals(code))
        {
            return "待支付";
        }
        if ("1".equals(code))
        {
            return "已支付";
        }
        if ("2".equals(code))
        {
            return "已退款";
        }
        return "未知支付状态";
    }

    private String consultStatusText(Object value)
    {
        String code = trimToEmpty(value);
        if ("0".equals(code))
        {
            return "待处理";
        }
        if ("1".equals(code))
        {
            return "处理中";
        }
        if ("2".equals(code))
        {
            return "已回复";
        }
        return "未知状态";
    }

    private String formatTime(Object value)
    {
        if (value instanceof Date)
        {
            return TIME_FMT.format((Date) value);
        }
        return trimToEmpty(value);
    }

    private String toMoney(Object value)
    {
        return toBigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
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
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text);
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
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private String trimToEmpty(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String truncate(String text, int maxLength)
    {
        String value = trimToEmpty(text);
        if (value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
