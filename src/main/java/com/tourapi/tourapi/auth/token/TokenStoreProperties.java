package com.tourapi.tourapi.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "token.store")
public class TokenStoreProperties {
    private String impl = "lua"; // sync or lua

    public String getImpl() {
        return impl;
    }

    public void setImpl(String impl) {
        this.impl = impl;
    }
}


