package com.bishe.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agri.ai")
public class AgriAiProperties
{
    private boolean enabled;

    private String provider = "openai-compatible";

    private String baseUrl = "https://api.deepseek.com";

    private String chatPath = "/chat/completions";

    private String apiKey;

    private String model = "deepseek-chat";

    private Double temperature = 0.3D;

    private Integer connectTimeoutMs = 10000;

    private Integer readTimeoutMs = 20000;

    private Integer maxHistory = 8;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getChatPath()
    {
        return chatPath;
    }

    public void setChatPath(String chatPath)
    {
        this.chatPath = chatPath;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }

    public Integer getConnectTimeoutMs()
    {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(Integer connectTimeoutMs)
    {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public Integer getReadTimeoutMs()
    {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(Integer readTimeoutMs)
    {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Integer getMaxHistory()
    {
        return maxHistory;
    }

    public void setMaxHistory(Integer maxHistory)
    {
        this.maxHistory = maxHistory;
    }
}
