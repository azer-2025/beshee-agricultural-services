package com.bishe.web.controller.agri;

import com.bishe.common.annotation.Anonymous;
import com.bishe.system.service.impl.AgriMallPayService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/order/pay")
public class AgriPayCallbackController
{
    @Autowired
    private AgriMallPayService agriMallPayService;

    @Anonymous
    @PostMapping(value = "/notify/alipay", produces = MediaType.TEXT_PLAIN_VALUE)
    public String alipayNotify(HttpServletRequest request)
    {
        return agriMallPayService.handleAlipayNotify(resolveParams(request));
    }

    @Anonymous
    @GetMapping(value = "/return/alipay", produces = MediaType.TEXT_HTML_VALUE)
    public String alipayReturn(HttpServletRequest request)
    {
        Map<String, Object> result = agriMallPayService.handleAlipayReturn(resolveParams(request));
        boolean success = Boolean.TRUE.equals(result.get("success"));
        String orderNo = escapeHtml(String.valueOf(result.get("orderNo") == null ? "" : result.get("orderNo")));
        String message = escapeHtml(String.valueOf(result.get("message") == null ? "" : result.get("message")));
        String title = success ? "支付成功" : "支付未完成";
        String tip = success
            ? "订单已支付成功，请返回前台页面查看最新状态。"
            : "支付回跳已接收，请返回前台订单页刷新后确认状态。";
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + title + "</title>"
            + "<style>body{font-family:Arial,Helvetica,sans-serif;padding:32px;color:#1f2937}"
            + ".card{max-width:680px;margin:0 auto;border:1px solid #e5e7eb;border-radius:12px;padding:24px}"
            + ".ok{color:#16a34a}.warn{color:#ea580c}.muted{color:#6b7280}</style></head><body><div class=\"card\">"
            + "<h2 class=\"" + (success ? "ok" : "warn") + "\">" + title + "</h2>"
            + "<p>订单号：" + (orderNo.isEmpty() ? "-" : orderNo) + "</p>"
            + "<p>" + (message.isEmpty() ? tip : message) + "</p>"
            + "<p class=\"muted\">" + tip + "</p>"
            + "</div></body></html>";
    }

    private Map<String, String> resolveParams(HttpServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        if (request == null || request.getParameterMap() == null)
        {
            return params;
        }
        request.getParameterMap().forEach((key, values) -> {
            String joined = values == null ? "" : Arrays.stream(values).collect(Collectors.joining(","));
            params.put(key, joined);
        });
        return params;
    }

    private String escapeHtml(String text)
    {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
