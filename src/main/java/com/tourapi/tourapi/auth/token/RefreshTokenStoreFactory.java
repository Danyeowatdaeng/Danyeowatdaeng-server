package com.tourapi.tourapi.auth.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
@EnableConfigurationProperties(TokenStoreProperties.class)
public class RefreshTokenStoreFactory {

    @Bean
    public TokenHasher tokenHasher(@Value("${token.hmac-secret}") String secret) {
        return new TokenHasher(secret);
    }

    @Bean
    public RefreshTokenStore refreshTokenStore(TokenStoreProperties props,
                                               RedisTemplate<String, String> redisTemplate,
                                               TokenHasher tokenHasher,
                                               DefaultRedisScript<String> rotateScript,
                                               @Value("${token.refresh.ttl-seconds}") long ttlSeconds) {
        String impl = props.getImpl();
        if ("sync".equalsIgnoreCase(impl)) {
            return new SynchronizedRedisStore(redisTemplate, tokenHasher, ttlSeconds);
        }
        return new LuaAtomicRedisStore(redisTemplate, tokenHasher, rotateScript, ttlSeconds);
    }
}


