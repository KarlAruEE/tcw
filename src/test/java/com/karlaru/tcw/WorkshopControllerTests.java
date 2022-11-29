package com.karlaru.tcw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkshopControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void shouldReturnWorkshopList(){
        webTestClient
                .get().uri("/api/v1/workshop")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty()
                    .jsonPath("$[0].name").isEqualTo("London")
                    .jsonPath("$[0].address").isEqualTo("1A Gunton Rd, London")
                    .jsonPath("$[0].vehicles").isEqualTo("CAR")
                    .jsonPath("$[1].name").isEqualTo("Manchester")
                    .jsonPath("$[1].address").isEqualTo("14 Bury New Rd, Manchester")
                    .jsonPath("$[1].vehicles").isArray()
                    .jsonPath("$[1].vehicles[0]").isEqualTo("CAR")
                    .jsonPath("$[1].vehicles[1]").isEqualTo("TRUCK");
    }
}
