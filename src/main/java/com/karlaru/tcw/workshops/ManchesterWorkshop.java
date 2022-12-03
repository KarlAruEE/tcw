package com.karlaru.tcw.workshops;


import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.ErrorException;
import com.karlaru.tcw.exceptions.UnprocessableEntityException;
import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

@Service
public class ManchesterWorkshop implements WorkshopInterface {

    @Value("${MANCHESTER_URL:http://localhost:9004/api/v2/tire-change-times}")
    private String manchesterUrl;
    private final WebClient webClient;

    private final Workshop workshop =
            new Workshop("Manchester", "14 Bury New Rd, Manchester", List.of(Workshop.VehicleType.Car, Workshop.VehicleType.Truck));

    public ManchesterWorkshop(@Lazy WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void setMockUrl(String mockUrl){
        this.manchesterUrl = mockUrl;
    }

    @Override
    public Workshop getWorkshop(){
        return this.workshop;
    }

    @Override
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
        String getUrl = String.format("%s?from=%s", manchesterUrl, from);

        ZonedDateTime untilZonedDateTime = ZonedDateTime.parse(until + "T00:00:00Z");

        if (untilZonedDateTime.isBefore(ZonedDateTime.parse(from+"T00:00:00Z")))
            return Flux.error(new BadRequestException(HttpStatus.BAD_REQUEST.value(), "Until date is before from"));

        return webClient
                .get()
                .uri(getUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(BadRequestException.class))
                .bodyToFlux(AvailableChangeTime.class)
                .map(m -> {
                    m.setWorkshop(workshop);
                    return m;
                })
                .filter(f -> f.getTime().isBefore(untilZonedDateTime))
                .onErrorMap(Predicate.not(BadRequestException.class::isInstance),
                        throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(), workshop.name()+" REST api seems to be offline"));
    }

    @Override
    public Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation) {
        return webClient
                .post()
                .uri(manchesterUrl+"/{id}/booking", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .onStatus(HttpStatus::isError,
                        clientResponse -> {
                            if(clientResponse.statusCode().value()==HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                                return clientResponse.bodyToMono(UnprocessableEntityException.class);
                            }
                            else {
                                return clientResponse.bodyToMono(BadRequestException.class);
                            }
                        })
                .bodyToMono(Booking.class)
                .map(booking -> {
                    booking.setWorkshop(workshop);
                    return booking;
                })
                .onErrorMap(Predicate.not(BadRequestException.class::isInstance).and(Predicate.not(UnprocessableEntityException.class::isInstance)),
                        throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(), workshop.name()+" REST api seems to be offline"));
    }
}
