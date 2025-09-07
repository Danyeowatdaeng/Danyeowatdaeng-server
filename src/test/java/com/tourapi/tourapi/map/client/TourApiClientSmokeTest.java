package com.tourapi.tourapi.map.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.tourapi.tourapi.map.config.TourApiProperties;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

public class TourApiClientSmokeTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void locationBased_smoke() {
        // given
        String body = "{\n" +
                "  \"resultCode\": \"0000\",\n" +
                "  \"resultMsg\": \"OK\",\n" +
                "  \"items\": []\n" +
                "}";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        String baseUrl = server.url("/KorPetTourService").toString();

        TourApiProperties props = new TourApiProperties();
        props.setBaseUrl(baseUrl.substring(0, baseUrl.length() - 1)); // remove trailing slash
        props.setServiceKey("dummy");
        props.setLocationBasedListPath("/locationBasedList");

        WebClient webClient = WebClient.builder().baseUrl(props.getBaseUrl()).build();
        TourApiClient client = new TourApiClient(webClient, props);

        // when
        ExternalTourApiResponse res = client.fetchTourDataByLocation(37.5, 127.0, 1000, 12, true);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getResultCode()).isEqualTo("0000");
        assertThat(res.getItems()).isEmpty();
    }
}


