package com.karlaru.tcw;

import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.ErrorException;
import com.karlaru.tcw.exceptions.UnprocessableEntityException;
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
import org.springframework.test.util.ReflectionTestUtils;
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
    private static final ContactInformation contactInformation = new ContactInformation("Back in London");

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s/api/v1/tire-change-times", mockWebServer.getPort());
        ReflectionTestUtils.setField(londonWorkshop, "londonUrl", baseUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnAvailableTimes() {
        AvailableChangeTime testTime1 = new AvailableChangeTime(ZonedDateTime.parse("2022-11-30T08:00:00Z"),"79c840f9-d8af-439a-a755-223d6582fa98");
        testTime1.setWorkshop(londonWorkshop.getWorkshop());
        AvailableChangeTime testTime2 = new AvailableChangeTime(ZonedDateTime.parse("2022-11-30T09:00:00Z"),"79c840f9-d8af-439a-a755-223d6582fa99");
        testTime2.setWorkshop(londonWorkshop.getWorkshop());

        String remoteApiResponse =  "<tireChangeTimesResponse>" +
                                    "  <availableTime>" +
                                    "    <uuid>79c840f9-d8af-439a-a755-223d6582fa98</uuid>" +
                                    "    <time>2022-11-30T08:00:00Z</time>" +
                                    "  </availableTime>" +
                                    "  <availableTime>" +
                                    "    <uuid>79c840f9-d8af-439a-a755-223d6582fa99</uuid>" +
                                    "    <time>2022-11-30T09:00:00Z</time>" +
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
                                e.getId().equals(testTime1.getId()) &&
                                e.isAvailable() &&
                                e.getWorkshop() == testTime1.getWorkshop() &&
                                e.getTime().isEqual(testTime1.getTime()))
                .expectNextMatches(e ->
                        e.getId().equals(testTime2.getId()) &&
                                e.isAvailable() &&
                                e.getWorkshop() == testTime2.getWorkshop() &&
                                e.getTime().isEqual(testTime2.getTime()))
                .verifyComplete();
    }

    @Test
    public void shouldReturnEmptyAvailableTimes() {

        String remoteApiResponse =
                "<tireChangeTimesResponse>" +
                "</tireChangeTimesResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = londonWorkshop.getAvailableChangeTime("2002-11-30", "2002-12-01");

        StepVerifier
                .create(response)
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldReturnBadRequest() {

        String remoteApiResponse =  "<errorResponse>"+
                                    "  <statusCode>400</statusCode>"+
                                    "  <error>bad req 1</error>"+
                                    "</errorResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = londonWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("bad req 1"))
                .verify();
    }

    @Test
    public void shouldReturnInSeEr() {

        String remoteApiResponse =  "<errorResponse>"+
                                    "  <statusCode>500</statusCode>"+
                                    "  <error>Internal Serv Error</error>"+
                                    "</errorResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = londonWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("Internal Serv Error"))
                .verify();
    }

    @Test
    public void shouldReturnApiOffline() {

        String remoteApiResponse =  "<tireChangeXXXXTimesResponse>" +
                                    "  <availableTimer>" +
                                    "    <id>79c840f9-d8af-439a-a755-223d6582fa98</uuid>" +
                                    "    <timed>2022-11-30T08:00:00Z</time>" +
                                    "  </availableTimes>" +
                                    "</tireChangeTimesResponses>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = londonWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("London REST api seems to be offline"))
                .verify();
    }

    @Test
    public void shouldBookAvailableTime(){

        String remoteApiResponse =  "<tireChangeBookingResponse>" +
                                    "  <uuid>79c840f9-d8af-439a-a755-223d6582fa98</uuid>" +
                                    "  <time>2022-11-30T08:00:00Z</time>" +
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
                                e.getId().equals("79c840f9-d8af-439a-a755-223d6582fa98") &&
                                e.getTime().equals("2022-11-30T08:00:00Z"))
                .verifyComplete();
    }

    @Test
    public void shouldBookBadRequest(){

        String remoteApiResponse =  "<errorResponse>"+
                                    "  <statusCode>400</statusCode>"+
                                    "  <error>Bad Request</error>"+
                                    "</errorResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = londonWorkshop.bookChangeTime("79c840f9-d8af-439a-a755-223d6582fa98",
                Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("Bad Request"))
                .verify();
    }

    @Test
    public void shouldBookButBookedAlready(){

        String remoteApiResponse =  "<errorResponse>"+
                                    "  <statusCode>422</statusCode>"+
                                    "  <error>tire change time  is unavailable</error>"+
                                    "</errorResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = londonWorkshop.bookChangeTime("79c840f9-d8af-439a-a755-223d6582fa98",
                Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof UnprocessableEntityException &&
                        ((UnprocessableEntityException) throwable).getExceptionData().getMessage().equals("tire change time  is unavailable"))
                .verify();
    }

    @Test
    public void shouldBookButInSeEr(){

        String remoteApiResponse =  "<errorResponse>"+
                                    "  <statusCode>500</statusCode>"+
                                    "  <error>In Se Er</error>"+
                                    "</errorResponse>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = londonWorkshop.bookChangeTime("79c840f9-d8af-439a-a755-223d6582fa98",
                Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("In Se Er"))
                .verify();
    }
    @Test
    public void shouldBookButApiOffline(){

        String remoteApiResponse =  "<tireChangeBookingResponses>" +
                                    "  <uuids>79c840f9-d8af-439a-a755-223d6582fa98</uuid>" +
                                    "  <timed>2022-11-30T08:00:00Z</time>" +
                                    "</tireChangeBookingResponses>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = londonWorkshop.bookChangeTime("79c840f9-d8af-439a-a755-223d6582fa98",
                Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("London REST api seems to be offline"))
                .verify();
    }
}
