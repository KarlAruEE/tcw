package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.Workshop;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    private WebClient webClient;
    private Iterable<Workshop> workshops;

    @GetMapping
    public Flux<Workshop> getWorkshop(){
        return Flux.fromIterable(workshops);
    }

}
