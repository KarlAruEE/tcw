package com.karlaru.tcw.workshops;


import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.ErrorException;
import com.karlaru.tcw.exceptions.UnprocessableEntityException;
import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.response.models.XMLChangeTimesResponse;
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
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class LondonWorkshop implements WorkshopInterface {

    @Value("${LONDON_URL:http://localhost:9003/api/v1/tire-change-times}")
    private String londonUrl;
    private final WebClient webClient;

    private final Workshop workshop =
            new Workshop("London", "1A Gunton Rd, London", List.of(Workshop.VehicleType.Car));

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
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(BadRequestException.class))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(ErrorException.class))
                .bodyToMono(XMLChangeTimesResponse.class)
                .filter(xmlChangeTimesResponse -> Objects.nonNull(xmlChangeTimesResponse.getAvailableTime()))
                .map(XMLChangeTimesResponse::getAvailableTime)
                .flatMapIterable(list -> list)
                .map(s -> new AvailableChangeTime(ZonedDateTime.parse(s.getTime()), s.getUuid()))
                .map(m -> {
                    m.setWorkshop(workshop);
                    return m;
                })
                .onErrorMap(Predicate.not(BadRequestException.class::isInstance)
                       .and(Predicate.not(ErrorException.class::isInstance)),
                        throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(), workshop.name()+" REST api seems to be offline"));
    }

    @Override
    public Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation) {
        return webClient
                .put()
                .uri(londonUrl+"/{id}/booking", id)
                .contentType(MediaType.TEXT_XML)
                .accept(MediaType.TEXT_XML)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> {
                            if(clientResponse.statusCode().value()==HttpStatus.BAD_REQUEST.value()) {
                                return clientResponse.bodyToMono(BadRequestException.class);
                            }
                            else {
                                return clientResponse.bodyToMono(UnprocessableEntityException.class);
                            }
                        })
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(ErrorException.class))
                .bodyToMono(Booking.class)
                .map(booking -> {
                    booking.setWorkshop(workshop);
                    return booking;
                })
                .onErrorMap(Predicate.not(BadRequestException.class::isInstance)
                                .and(Predicate.not(UnprocessableEntityException.class::isInstance)
                                        .and(Predicate.not(ErrorException.class::isInstance))),
                        throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(), workshop.name()+" REST api seems to be offline"));
    }
}
