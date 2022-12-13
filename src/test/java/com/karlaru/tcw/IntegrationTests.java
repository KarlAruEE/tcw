package com.karlaru.tcw;

import com.karlaru.tcw.workshops.LondonWorkshop;
import com.karlaru.tcw.workshops.ManchesterWorkshop;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class IntegrationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ManchesterWorkshop manchesterWorkshop;

    private final GenericContainer<?> manchesterApi =
            new GenericContainer<>(DockerImageName.parse("surmus/manchester-tire-workshop:2.0.1"))
                    .withExposedPorts(80);

    @Autowired
    private LondonWorkshop londonWorkshop;

    private final GenericContainer<?> londonApi =
            new GenericContainer<>(DockerImageName.parse("surmus/london-tire-workshop:2.0.1"))
                    .withExposedPorts(80);

    @BeforeAll
    public void setUp(){

        manchesterApi.start();
        String manchesterUrl = String.format("http://%s:%s/api/v2/tire-change-times", manchesterApi.getHost(), manchesterApi.getFirstMappedPort());
        ReflectionTestUtils.setField(manchesterWorkshop, "url", manchesterUrl);

        londonApi.start();
        String londonUrl = String.format("http://%s:%s/api/v1/tire-change-times", londonApi.getHost(), londonApi.getFirstMappedPort());
        ReflectionTestUtils.setField(londonWorkshop, "url", londonUrl);
    }

    @Test
    public void shouldReturnManchesterAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/Manchester/tire-change-times?from=2002-01-01&until=2003-01-01&vehicles=Truck")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray();
    }
    @Test
    public void shouldReturnLondonAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/London/tire-change-times?from=2012-01-01&until=2023-01-01&vehicles=Car")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray();
    }
    @Test
    public void shouldReturnAllAvailableTimes(){
        webTestClient
                .get().uri("/api/v1/workshop/London,Manchester/tire-change-times?from=2012-01-01&until=2023-01-01&vehicles=Car")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$").isArray();
    }
    @Test
    public void shouldReturnAllAvailableTimesAllVehicles(){
        webTestClient
                .get().uri("/api/v1/workshop/London,Manchester/tire-change-times?from=2012-01-01&until=2023-01-01&vehicles=Car,Truck")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }
}
