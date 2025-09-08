package com.tourapi.tourapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class TourApiConfig {

    private static final Logger log = LoggerFactory.getLogger(TourApiConfig.class);

    @Bean
    public WebClient tourApiWebClient(TourApiProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + "," + MediaType.APPLICATION_XML_VALUE)
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    if (log.isDebugEnabled()) {
                        log.debug("[TourAPI] Request: {} {}", clientRequest.method(), clientRequest.url());
                    }
                    return Mono.just(clientRequest);
                }))
                .build();
    }
}


