package com.tourapi.tourapi.petAvatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petavatar")
public class PetAvatarProperties {

    private boolean enabled = true;
    private String provider = "dalle"; // dalle | mock | custom
    private long timeoutMs = 30000L;
    private String providerBaseUrl = "https://api.openai.com/v1";
    private String apiKey;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    public String getProviderBaseUrl() { return providerBaseUrl; }
    public void setProviderBaseUrl(String providerBaseUrl) { this.providerBaseUrl = providerBaseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}

 