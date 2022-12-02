package com.karlaru.tcw;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.workshops.LondonWorkshop;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
public class LondonWorkshopTest {


    private static MockWebServer mockWebServer;
    private static final LondonWorkshop londonWorkshop = new LondonWorkshop(WebClient.builder().build());

    private static final AvailableChangeTime testTime = new AvailableChangeTime(ZonedDateTime.parse("2022-11-30T08:00:00Z"),
                                                                                    "79c840f9-d8af-439a-a755-223d6582fa98");

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s/api/v1/tire-change-times", mockWebServer.getPort());
        londonWorkshop.setMockUrl(baseUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnAvailableTimes() {

        testTime.setWorkshop(londonWorkshop.getWorkshop());


        String remoteApiResponse =  "<tireChangeTimesResponse>" +
                                    "  <availableTime>" +
                                    "    <uuid>79c840f9-d8af-439a-a755-223d6582fa98</uuid>" +
                                    "    <time>2022-11-30T08:00:00Z</time>" +
                                    "  </availableTime>" +
                                    "</tireChangeTimesResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = londonWorkshop.getAvailableChangeTime("2022-11-30", "2022-12-01");

        StepVerifier
                .create(response)
                .expectNextMatches(e ->
                                e.getUuid().equals(testTime.getUuid()) &&
                                e.isAvailable() &&
                                e.getWorkshop() == testTime.getWorkshop() &&
                                e.getTime().isEqual(testTime.getTime()))
                .verifyComplete();
    }

    @Test
    public void shouldBookAvailableTime(){

        ContactInformation contactInformation = new ContactInformation("Back in London");

        String remoteApiResponse =  "<tireChangeBookingResponse>\n" +
                                    "  <uuid>79c840f9-d8af-439a-a755-223d6582fa98</uuid>\n" +
                                    "  <time>2022-11-30T08:00:00Z</time>\n" +
                                    "</tireChangeBookingResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = londonWorkshop.bookChangeTime("79c840f9-d8af-439a-a755-223d6582fa98",
                                                                Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectNextMatches(e ->
                                e.getId().equals(testTime.getUuid()) &&
                                ZonedDateTime.parse(e.getTime()).isEqual(testTime.getTime()))
                .verifyComplete();
    }
}
