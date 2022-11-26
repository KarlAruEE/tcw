package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.AvailableChangeTime;
import com.karlaru.tcw.models.Workshop;
import com.karlaru.tcw.xmlmodels.ChangeTimes;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


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

    @GetMapping("/{workshopName}")
    public Mono<Workshop> getAWorkshop(@PathVariable("workshopName") String workshopName){
        Optional<Workshop> workshop = workshops.stream()
                .filter(w -> (Objects.equals(w.getName(), workshopName)))
                .findAny();
        return workshop.map(Mono::just).orElseGet(Mono::empty);
    }

    @GetMapping(value = "/tire-change-times")
    public Flux<Object> getAvailableTimes(){

        Flux<AvailableChangeTime> manchester = webClient
                .get()
                .uri("http://localhost:9004/api/v2/tire-change-times")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(AvailableChangeTime.class)
                .map(m -> {
                    m.setWorkshop(workshops.get(0));
                    return m;
                });

        Flux<AvailableChangeTime> london =  webClient
                .get()
                .uri("http://localhost:9003/api/v1/tire-change-times/available?from=2006-01-02&until=2030-01-02")
                .accept(MediaType.TEXT_XML)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(ChangeTimes.class)
                .map(ChangeTimes::getAvailableTime)
                .flatMapIterable(list -> list)
                .map(s -> new AvailableChangeTime(s.getTime(), s.getUuid()))
                .map(m -> {
                    m.setWorkshop(workshops.get(1));
                    return m;
                });

        return Flux.concat(manchester, london);

    }
}