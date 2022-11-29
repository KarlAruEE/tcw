package com.karlaru.tcw.workshops;

import com.karlaru.tcw.models.*;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public LondonWorkshop(@Autowired WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getWorkshopName(){
        return this.workshop.getName();
    }

    @Override
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
        String getUrl = String.format("%s/available?from=%s&until=%s", londonUrl, from, until);

        return webClient
                .get()
                .uri(getUrl)
                .accept(MediaType.TEXT_XML)
                .retrieve()
                .bodyToMono(XMLChangeTimes.class)
                .map(XMLChangeTimes::getAvailableTime)
                .flatMapIterable(list -> list)
                .map(s -> new AvailableChangeTime(ZonedDateTime.parse(s.getTime()), s.getUuid()))
                .map(m -> {
                    m.setWorkshop(workshop);
                    return m;
                });
    }

    @Override
    public Mono<BookingResponse> bookChangeTime(Object id, Mono<ContactInformation> contactInformation) {
        return webClient
                .put()
                .uri(londonUrl+"/"+id+"/booking")
                .contentType(MediaType.TEXT_XML)
                .accept(MediaType.TEXT_XML)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .bodyToMono(BookingResponse.class);
    }
}
