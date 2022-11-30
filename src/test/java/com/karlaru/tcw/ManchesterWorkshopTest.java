package com.karlaru.tcw;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.workshops.ManchesterWorkshop;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
public class ManchesterWorkshopTest {


    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnAvailableTimes() {

        ManchesterWorkshop manchesterWorkshop = new ManchesterWorkshop(WebClient.builder().build());
        String baseUrl = String.format("http://localhost:%s/api/v2/tire-change-times", mockWebServer.getPort());
        manchesterWorkshop.setTestWebClient(baseUrl);

        AvailableChangeTime testTime1 = new AvailableChangeTime(true, 5, ZonedDateTime.parse("2022-11-23T12:00:00Z"));
        testTime1.setWorkshop(manchesterWorkshop.getWorkshop());

        AvailableChangeTime testTime2 = new AvailableChangeTime(false, 13, ZonedDateTime.parse("2022-11-24T01:00:00Z"));
        testTime2.setWorkshop(manchesterWorkshop.getWorkshop());

        String remoteApiResponse = "[" +
                                        "{" +
                                            "\"id\":5," +
                                            "\"time\":\"2022-11-23T12:00:00Z\"," +
                                            "\"available\":true" +
                                        "}," +
                                        "{" +
                                            "\"id\":13," +
                                            "\"time\":\"2022-11-24T01:00:00Z\"," +
                                            "\"available\":false" +
                                        "}" +
                                    "]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectNextMatches(e ->
                                e.getId() == testTime1.getId() &&
                                e.isAvailable() &&
                                e.getWorkshop() == testTime1.getWorkshop() &&
                                e.getTime().isEqual(testTime1.getTime()))
                .expectNextMatches(e ->
                                e.getId() == testTime2.getId() &&
                                !e.isAvailable() &&
                                e.getWorkshop() == testTime2.getWorkshop() &&
                                e.getTime().isEqual(testTime2.getTime()))
                .verifyComplete();
    }
}
