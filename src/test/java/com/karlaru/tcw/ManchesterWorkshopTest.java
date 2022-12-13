package com.karlaru.tcw;

import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.ErrorException;
import com.karlaru.tcw.exceptions.UnprocessableEntityException;
import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.workshops.ManchesterWorkshop;
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
public class ManchesterWorkshopTest {


    private static MockWebServer mockWebServer;
    private static final ManchesterWorkshop manchesterWorkshop = new ManchesterWorkshop(WebClient.builder().build());
    private static final ContactInformation contactInformation = new ContactInformation("Back in Black");
    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s/api/v2/tire-change-times", mockWebServer.getPort());
        ReflectionTestUtils.setField(manchesterWorkshop, "url", baseUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnAvailableTimes() {

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
                                e.getId().equals(testTime1.getId()) &&
                                e.isAvailable() &&
                                e.getWorkshop() == testTime1.getWorkshop() &&
                                e.getTime().isEqual(testTime1.getTime()))
                .expectNextMatches(e ->
                                e.getId().equals(testTime2.getId()) &&
                                !e.isAvailable() &&
                                e.getWorkshop() == testTime2.getWorkshop() &&
                                e.getTime().isEqual(testTime2.getTime()))
                .verifyComplete();
    }

    @Test
    public void shouldReturnBadRequest() {

        String remoteApiResponse =
                "{" +
                    "\"code\": \"400\"," +
                    "\"message\":\"bad request 1\"" +
                "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("bad request 1"))
                .verify();
    }

    @Test
    public void shouldReturnBadDateError() {

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-01", "2022-11-2");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("Invalid date format"))
                .verify();
    }

    @Test
    public void shouldReturnUntilBeforeFromError() {

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-15", "2022-11-02");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("From date is after Until date"))
                .verify();
    }

    @Test
    public void shouldReturnInternalServerError() {

        String remoteApiResponse =
                "{" +
                    "\"code\": \"500\"," +
                    "\"message\":\"Server error 1\"" +
                "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                     ((ErrorException) throwable).getExceptionData().getMessage().equals("Server error 1"))
                .verify();
    }

    @Test
    public void shouldReturnServerOfflineError() {

        // response is with bad format for bodyToFlux or server is offline
        String remoteApiResponse =
                "[" +
                    "{" +
                        "\"id67\":5," +
                        "\"time-xx\":\"2022-11-23T12:00:00Z\"," +
                        "\"ava\":true" +
                    "}" +
                "]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Flux<AvailableChangeTime> response = manchesterWorkshop.getAvailableChangeTime("2022-11-23", "2022-11-25");

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("Manchester REST api seems to be offline"))
                .verify();
    }

    @Test
    public void shouldBookAvailableTime(){
        AvailableChangeTime testTime = new AvailableChangeTime(true, 5, ZonedDateTime.parse("2022-11-23T12:00:00Z"));

        String remoteApiResponse = "{" +
                                        "\"id\":5," +
                                        "\"time\":\"2022-11-23T12:00:00Z\"," +
                                        "\"available\":false" +
                                    "},";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = manchesterWorkshop.bookChangeTime("5", Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectNextMatches(e ->
                                e.getId().equals(String.valueOf(testTime.getId())) &&
                                !e.isAvailable() &&
                                ZonedDateTime.parse(e.getTime()).isEqual(testTime.getTime()))
                .verifyComplete();

    }

    @Test
    public void shouldReturnBadPostRequest() {

        String remoteApiResponse =
                "{" +
                        "\"code\": \"11\"," +
                        "\"message\":\"strconv.ParseUint: parsing -1: invalid syntax\"" +
                "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = manchesterWorkshop.bookChangeTime("-1", Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        ((BadRequestException) throwable).getExceptionData().getMessage().equals("strconv.ParseUint: parsing -1: invalid syntax"))
                .verify();
    }

    @Test
    public void shouldReturnUnprocessedPostRequest() {

        String remoteApiResponse =
                "{" +
                        "\"code\": \"22\"," +
                        "\"message\":\"tire change time 11 is unavailable\"" +
                "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = manchesterWorkshop.bookChangeTime("11", Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof UnprocessableEntityException &&
                        ((UnprocessableEntityException) throwable).getExceptionData().getMessage().equals("tire change time 11 is unavailable"))
                .verify();
    }

    @Test
    public void shouldReturnInSeErPostRequest() {

        String remoteApiResponse =
                "{" +
                        "\"code\": \"500\"," +
                        "\"message\":\"internal server error\"" +
                 "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = manchesterWorkshop.bookChangeTime("1", Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("internal server error"))
                .verify();
    }
    @Test
    public void shouldReturnApiDownPostRequest() {

        String remoteApiResponse =
                "{" +
                        "\"code\": \"500\"," +
                        "\"message\":\"Manchester REST api seems to be offline\"" +
                "}" ;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(remoteApiResponse));

        Mono<Booking> response = manchesterWorkshop.bookChangeTime("1", Mono.just(contactInformation));

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof ErrorException &&
                        ((ErrorException) throwable).getExceptionData().getMessage().equals("Manchester REST api seems to be offline"))
                .verify();
    }

}
