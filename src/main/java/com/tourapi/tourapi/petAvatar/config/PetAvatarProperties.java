package com.tourapi.tourapi.petAvatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "petavatar")
public class PetAvatarProperties {

    private boolean enabled = true;
    private String provider = "gemini"; // gemini | dalle | mock | custom
    private long timeoutMs = 30000L;
    private String providerBaseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String apiKey;

    // Vertex AI (Imagen) 설정
    private String vertexProjectId;
    private String vertexLocation = "us-central1";
    private String vertexModel = "imagegeneration@006"; // 최신 모델명은 콘솔 확인 필요
    private String vertexAccessToken; // Service Account로 발급된 OAuth2 Bearer

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

    public String getVertexProjectId() { return vertexProjectId; }
    public void setVertexProjectId(String vertexProjectId) { this.vertexProjectId = vertexProjectId; }

    public String getVertexLocation() { return vertexLocation; }
    public void setVertexLocation(String vertexLocation) { this.vertexLocation = vertexLocation; }

    public String getVertexModel() { return vertexModel; }
    public void setVertexModel(String vertexModel) { this.vertexModel = vertexModel; }

    public String getVertexAccessToken() { return vertexAccessToken; }
    public void setVertexAccessToken(String vertexAccessToken) { this.vertexAccessToken = vertexAccessToken; }
}

 