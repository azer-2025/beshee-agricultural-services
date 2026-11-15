package com.bishe.system.service;

import java.util.List;
import java.util.Map;

public interface IAgriAiService
{
    Map<String, Object> chat(Long buyerId, Map<String, Object> form);

    List<Map<String, Object>> listHistory(Long buyerId, Integer limit);

    int clearHistory(Long buyerId);

    List<Map<String, Object>> listAdminHistory(Map<String, Object> query);
}
