package com.tourapi.tourapi.web.controller.storage;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageHealthController {

    private static final Logger log = LoggerFactory.getLogger(StorageHealthController.class);

    @Value("${aws.s3.cloudfrontDomain:}")
    private String cloudfrontDomain;

    @GetMapping("/health")
    public ResponseEntity<?> health(@RequestParam(value = "key", required = false) String key) {
        Map<String, Object> result = new HashMap<>();

        boolean hasCdnConfig = StringUtils.hasText(cloudfrontDomain);
        result.put("cloudfrontConfigured", hasCdnConfig);
        result.put("cloudfrontDomain", cloudfrontDomain);

        if (!hasCdnConfig) {
            return ApiResponse.onSuccess(SuccessStatus.OK, result);
        }

        String url = buildCdnUrl(key);
        result.put("probeUrl", url);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> resp = restTemplate.exchange(URI.create(url), HttpMethod.HEAD, entity, Void.class);
            result.put("reachable", true);
            result.put("status", resp.getStatusCode().value());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid probe URL: {} - {}", url, e.getMessage());
            result.put("reachable", false);
            result.put("error", "Invalid URL: ensure cloudfrontDomain includes scheme, e.g., https://domain");
        } catch (RestClientException e) {
            log.warn("CDN health probe failed: {}", e.getMessage());
            result.put("reachable", false);
            result.put("error", e.getMessage());
        }

        return ApiResponse.onSuccess(SuccessStatus.OK, result);
    }


    private String buildCdnUrl(String key) {
        String base = ensureScheme(cloudfrontDomain);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = (key == null || key.isBlank()) ? "/robots.txt" : (key.startsWith("/") ? key : "/" + key);
        return base + path;
    }

    private static String ensureScheme(String domain) {
        if (domain == null || domain.isBlank()) return domain;
        String lower = domain.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) return domain;
        return "https://" + domain;
    }
}


