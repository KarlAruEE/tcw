package com.karlaru.tcw.workshops;


import com.karlaru.tcw.response.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class LondonWorkshop implements WorkshopInterface {

    @Value("${LONDON_URL:http://localhost:9003/api/v1/tire-change-times}")
    private String londonUrl;
    private final WebClient webClient;

    private final Workshop workshop =
            new Workshop("London", "1A Gunton Rd, London", List.of(Workshop.VehicleType.CAR));

    public LondonWorkshop(@Lazy WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void setMockUrl(String mockUrl){
        this.londonUrl = mockUrl;
    }

    @Override
    public Workshop getWorkshop(){
        return this.workshop;
    }

    @Override
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
        String getUrl = String.format("%s/available?from=%s&until=%s", londonUrl, from, until);

        return webClient
                .get()
                .uri(getUrl)
                .accept(MediaType.TEXT_XML)
                .retrieve()
                .bodyToMono(XMLChangeTimesResponse.class)
                .onErrorResume(clientResponse -> Mono.error(
                        new NotFoundException(HttpStatus.NOT_FOUND, "Workshop "+getWorkshop().name()+" returned 404")))
                .map(XMLChangeTimesResponse::getAvailableTime)
                .flatMapIterable(list -> list)
                .map(s -> new AvailableChangeTime(ZonedDateTime.parse(s.getTime()), s.getUuid()))
                .map(m -> {
                    m.setWorkshop(workshop);
                    return m;
                });
    }

    @Override
    public Mono<Booking> bookChangeTime(Object id, Mono<ContactInformation> contactInformation) {
        return webClient
                .put()
                .uri(londonUrl+"/{id}/booking", id)
                .contentType(MediaType.TEXT_XML)
                .accept(MediaType.TEXT_XML)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .bodyToMono(Booking.class);
    }
}
