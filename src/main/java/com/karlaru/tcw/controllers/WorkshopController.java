package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    @Value("${LONDON_URL:http://localhost:9003/api/v1/tire-change-times}")
    private String londonUrl;
    @Value("${MANCHESTER_URL:http://localhost:9004/api/v2/tire-change-times}")
    private String manchesterUrl;
    private final WebClient webClient;
    private final List<Workshop> workshops;

    public WorkshopController(@Autowired WebClient webClient, @Autowired List<Workshop> workshops) {
        this.webClient = webClient;
        this.workshops = workshops;
    }

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromIterable(workshops);
    }

    @GetMapping(value = "/{workshop}/tire-change-times")
    public Flux<AvailableChangeTime> getAvailableTimes( @PathVariable String workshop,
                                                        @RequestParam String from,
                                                        @RequestParam String until,
                                                        @RequestParam(required = false, defaultValue = "ALL") String vehicle){

        List<Workshop> workshopsToGetTimeFor = workshops.stream()
                .filter(w -> workshop.equals("All") || w.getName().equals(workshop))
                .filter(w -> vehicle.equals("ALL") || w.getVehicles().contains(Workshop.VehicleType.valueOf(vehicle)))
                .toList();

        Flux<AvailableChangeTime> result = Flux.empty();

        // Manchester
        if (workshopsToGetTimeFor.contains(workshops.get(0))){
            result = Flux.concat(result, getManchesterTimes(from,until));
        }

        // London
        if (workshopsToGetTimeFor.contains(workshops.get(1))){
            result = Flux.concat(result, getLondonTimes(from,until));
        }
        return result;

    }

    @PostMapping(value = "/{workshop}/tire-change-times/{id}/booking", consumes = "application/json")
    public Mono<AvailableChangeTime> bookAvailableTime( @PathVariable String workshop,
                                                        @PathVariable Object id,
                                                        @RequestBody Mono<ContactInformation> contactInformation){
        if (workshop.equals("Manchester")){
            return bookManchesterTime(id, contactInformation);
        } else if (workshop.equals("London")) {
            return bookLondonTime(id, contactInformation);
        }

        return Mono.just(new AvailableChangeTime());
    }

    private Mono<AvailableChangeTime> bookManchesterTime(Object id, Mono<ContactInformation> contactInformation){
        return webClient
                .post()
                .uri(manchesterUrl+"/"+id+"/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .bodyToMono(AvailableChangeTime.class);
    }
    private Mono<AvailableChangeTime> bookLondonTime(Object id, Mono<ContactInformation> contactInformation){
        return webClient
                .put()
                .uri(londonUrl+"/"+id+"/booking")
                .contentType(MediaType.TEXT_XML)
                .accept(MediaType.TEXT_XML)
                .body(contactInformation, ContactInformation.class)
                .retrieve()
                .bodyToMono(XMLBookingResponse.class)
                .map(m -> new AvailableChangeTime(ZonedDateTime.parse(m.getTime()), m.getUuid()));
    }

    private Flux<AvailableChangeTime> getManchesterTimes(String from, String until){

        String getUrl = String.format("%s?from=%s", manchesterUrl, from);
        ZonedDateTime untilZonedDateTime = ZonedDateTime.parse(until + "T00:00:00Z");

        return webClient
                .get()
                .uri(getUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(AvailableChangeTime.class)
                .map(m -> {
                    m.setWorkshop(workshops.get(0));
                    return m;
                })
                .filter(f -> f.getTime().isBefore(untilZonedDateTime));
    }

    private Flux<AvailableChangeTime> getLondonTimes(String from, String until){

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
                    m.setWorkshop(workshops.get(1));
                    return m;
                });
    }
}