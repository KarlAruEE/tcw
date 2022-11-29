package com.karlaru.tcw.workshops;

import com.karlaru.tcw.models.AvailableChangeTime;
import com.karlaru.tcw.models.BookingResponse;
import com.karlaru.tcw.models.ContactInformation;
import com.karlaru.tcw.models.Workshop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ManchesterWorkshop implements WorkshopInterface {

    @Value("${MANCHESTER_URL:http://localhost:9004/api/v2/tire-change-times}")
    private String manchesterUrl;
    private final WebClient webClient;

    private final Workshop workshop =
            new Workshop("Manchester", "14 Bury New Rd, Manchester", List.of(Workshop.VehicleType.CAR, Workshop.VehicleType.TRUCK));

    public ManchesterWorkshop(@Autowired WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Workshop getWorkshop(){
        return this.workshop;
    }

    @Override
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
        String getUrl = String.format("%s?from=%s", manchesterUrl, from);
        ZonedDateTime untilZonedDateTime = ZonedDateTime.parse(until + "T00:00:00Z");

        return webClient
                .get()
                .uri(getUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(AvailableChangeTime.class)
                .map(m -> {
                    m.setWorkshop(workshop);
                    return m;
                })
                .filter(f -> f.getTime().isBefore(untilZonedDateTime));
    }

    @Override
    public Mono<BookingResponse> bookChangeTime(Object id, Mono<ContactInformation> contactInformation) {
        return webClient
                .post()
                .uri(manchesterUrl+"/"+id+"/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .bodyToMono(BookingResponse.class);
    }
}
