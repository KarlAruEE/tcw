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
import java.time.format.DateTimeParseException;
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

        try {
            ZonedDateTime fromZDT = ZonedDateTime.parse(from + "T00:00:00Z");
            ZonedDateTime untilZDT = ZonedDateTime.parse(until + "T00:00:00Z");
            if (untilZDT.isBefore(fromZDT)) {
                return Flux.error(
                        new BadRequestException(HttpStatus.BAD_REQUEST.value(), "From date is after Until date"));
            }

            return webClient
                    .get()
                    .uri(getUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError,
                            clientResponse -> clientResponse.bodyToMono(BadRequestException.class))
                    .onStatus(HttpStatus::is5xxServerError,
                            clientResponse -> clientResponse.bodyToMono(ErrorException.class))
                    .bodyToFlux(AvailableChangeTime.class)
                    .map(m -> {
                        m.setWorkshop(workshop);
                        return m;
                    })
                    .filter(f -> f.getTime().isBefore(untilZDT))
                    .onErrorMap(Predicate.not(BadRequestException.class::isInstance)
                           .and(Predicate.not(ErrorException.class::isInstance)),
                            throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(), workshop.name()+" REST api seems to be offline"));
        }catch (DateTimeParseException e){
            return Flux.error(new BadRequestException(HttpStatus.BAD_REQUEST.value(), "Invalid date format"));
        }
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
