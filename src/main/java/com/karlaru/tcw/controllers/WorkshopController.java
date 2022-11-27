package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    @Value("${LONDON_URL:http://localhost:9003}")
    private String londonUrl;
    @Value("${MANCHESTER_URL:http://localhost:9004}")
    private String manchesterUrl;
    private final WebClient webClient;
    private final List<Workshop> workshops;

    public WorkshopController(WebClient webClient, List<Workshop> workshops) {
        this.webClient = webClient;
        this.workshops = workshops;
    }

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromIterable(workshops);
    }

    @GetMapping(value = "/tire-change-times")
    public Flux<AvailableChangeTime> getAllAvailableTimes(  @RequestParam String from,
                                                            @RequestParam String until){
        return Flux.concat(
                getLondonTimes(from, until),
                getManchesterTimes(from, until));

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
    public Mono<AvailableChangeTime> bookAvailableTime(@PathVariable String workshop,
                                          @PathVariable Object id,
                                          @RequestBody ContactInformation contactInformation){
        // Manchester
        if (workshop.equals("Manchester")){
            return webClient
                    .post()
                    .uri(manchesterUrl+"/api/v2/tire-change-times/"+id+"/booking")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(contactInformation))
                    .retrieve()
                    .bodyToMono(AvailableChangeTime.class);
        } else if (workshop.equals("London")) {
            return webClient
                    .put()
                    .uri(londonUrl+"/api/v1/tire-change-times/"+id+"/booking")
                    .contentType(MediaType.TEXT_XML)
                    .accept(MediaType.TEXT_XML)
                    .body(BodyInserters.fromValue(contactInformation))
                    .retrieve()
                    .bodyToMono(XMLBookingResponse.class)
                    .map(m -> new AvailableChangeTime(ZonedDateTime.parse(m.getTime()), m.getUuid()));

        }

        return Mono.just(new AvailableChangeTime());
    }

    private Flux<AvailableChangeTime> getManchesterTimes(String from, String until){

        String getUrl = String.format(
                "%s/api/v2/tire-change-times?from=%s", manchesterUrl, from);

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

        String getUrl = String.format(
                "%s/api/v1/tire-change-times/available?from=%s&until=%s", londonUrl, from, until);

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