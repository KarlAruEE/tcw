package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.AvailableChangeTime;
import com.karlaru.tcw.models.Workshop;
import com.karlaru.tcw.models.XMLChangeTimes;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    private WebClient webClient;
    private List<Workshop> workshops;

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromIterable(workshops);
    }

    @GetMapping(value = "/tire-change-times")
    public Flux<AvailableChangeTime> getAllAvailableTimes(  @RequestParam String from,
                                                            @RequestParam String until){
        return Flux.concat(
                getManchesterTimes(from,until),
                getLondonTimes(from,until));
    }

    @GetMapping(value = "/{workshop}/tire-change-times")
    public Flux<AvailableChangeTime> getAvailableTimes( @PathVariable String workshop,
                                                        @RequestParam String from,
                                                        @RequestParam String until){
        if (Objects.equals(workshop, "manchester"))
            return getManchesterTimes(from,until);
        else if (Objects.equals(workshop, "london")) {
            return getLondonTimes(from,until);
        }
        return Flux.empty();

    }

    private Flux<AvailableChangeTime> getManchesterTimes(String from, String until){

        String getUrl = String.format(
                "http://localhost:9004/api/v2/tire-change-times?from=%s", from);

        ZonedDateTime untilZonedDateTime = ZonedDateTime.parse(until + "T00:00:00Z");

        return webClient
                .get()
                .uri("http://localhost:9004/api/v2/tire-change-times")
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
                "http://localhost:9003/api/v1/tire-change-times/available?from=%s&until=%s", from, until);

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