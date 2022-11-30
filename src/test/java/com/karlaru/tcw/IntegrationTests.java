package com.karlaru.tcw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void shouldReturnManchesterAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/Manchester/tire-change-times?from=2012-01-01&until=2023-01-01&vehicle=ALL")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty();
    }
    @Test
    public void shouldReturnLondonAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/London/tire-change-times?from=2012-01-01&until=2023-01-01&vehicle=ALL")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty();
    }
    @Test
    public void shouldReturnAllAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/All/tire-change-times?from=2012-01-01&until=2023-01-01&vehicle=ALL")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty();
    }
}
