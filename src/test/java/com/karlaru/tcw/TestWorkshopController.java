package com.karlaru.tcw;

import com.karlaru.tcw.controllers.WorkshopController;
import com.karlaru.tcw.workshops.LondonWorkshop;
import com.karlaru.tcw.workshops.ManchesterWorkshop;
import com.karlaru.tcw.workshops.WorkshopInterface;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AllArgsConstructor
@SpringBootTest
public class TestWorkshopController {


    private final List<WorkshopInterface> workshopList = List.of(
            new LondonWorkshop(WebClient.builder().build()),
            new ManchesterWorkshop(WebClient.builder().build()));

    @Test
    public void shouldReturnWorkshopList(){
        WebTestClient webTestClient = WebTestClient.bindToController(new WorkshopController(workshopList)).build();
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
