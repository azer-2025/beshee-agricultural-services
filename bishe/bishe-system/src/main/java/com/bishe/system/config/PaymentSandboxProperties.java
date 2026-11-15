package com.bishe.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.sandbox")
public class PaymentSandboxProperties
{
    private boolean enabled;

    private boolean autoSuccess = true;

    private Alipay alipay = new Alipay();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isAutoSuccess()
    {
        return autoSuccess;
    }

    public void setAutoSuccess(boolean autoSuccess)
    {
        this.autoSuccess = autoSuccess;
    }

    public Alipay getAlipay()
    {
        return alipay;
    }

    public void setAlipay(Alipay alipay)
    {
        this.alipay = alipay;
    }

    public static class Alipay
    {
        private String appId;

        private String merchantPrivateKey;

        private String alipayPublicKey;

        private String gatewayUrl;

        private String notifyUrl;

        private String returnUrl;

        private String method = "alipay.trade.wap.pay";

        private String productCode = "QUICK_WAP_WAY";

        public String getAppId()
        {
            return appId;
        }

        public void setAppId(String appId)
        {
            this.appId = appId;
        }

        public String getMerchantPrivateKey()
        {
            return merchantPrivateKey;
        }

        public void setMerchantPrivateKey(String merchantPrivateKey)
        {
            this.merchantPrivateKey = merchantPrivateKey;
        }

        public String getAlipayPublicKey()
        {
            return alipayPublicKey;
        }

        public void setAlipayPublicKey(String alipayPublicKey)
        {
            this.alipayPublicKey = alipayPublicKey;
        }

        public String getGatewayUrl()
        {
            return gatewayUrl;
        }

        public void setGatewayUrl(String gatewayUrl)
        {
            this.gatewayUrl = gatewayUrl;
        }

        public String getNotifyUrl()
        {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl)
        {
            this.notifyUrl = notifyUrl;
        }

        public String getReturnUrl()
        {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl)
        {
            this.returnUrl = returnUrl;
        }

        public String getMethod()
        {
            return method;
        }

        public void setMethod(String method)
        {
            this.method = method;
        }

        public String getProductCode()
        {
            return productCode;
        }

        public void setProductCode(String productCode)
        {
            this.productCode = productCode;
        }
    }
}
