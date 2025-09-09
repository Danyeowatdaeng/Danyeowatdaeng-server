package com.tourapi.tourapi.auth.oauth.strategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class OAuthStrategyFactory {

    private final Map<String, OAuthProviderStrategy> strategies = new ConcurrentHashMap<>();

    public OAuthStrategyFactory(List<OAuthProviderStrategy> strategyList) {
        for (OAuthProviderStrategy s : strategyList) {
            strategies.put(s.providerName().toLowerCase(), s);
        }
    }

    public OAuthProviderStrategy get(String provider) {
        OAuthProviderStrategy s = strategies.get(provider.toLowerCase());
        if (s == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
        return s;
    }
}


