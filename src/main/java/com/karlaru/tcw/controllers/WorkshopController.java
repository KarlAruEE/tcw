package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.*;
import com.karlaru.tcw.workshops.WorkshopInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    @Value("${LONDON_URL:http://localhost:9003/api/v1/tire-change-times}")
    private String londonUrl;
    @Value("${MANCHESTER_URL:http://localhost:9004/api/v2/tire-change-times}")
    private String manchesterUrl;
    private final WebClient webClient;
    private final List<Workshop> workshops;

    private final List<? extends WorkshopInterface> workshopList;

    public WorkshopController(@Autowired WebClient webClient, @Autowired List<Workshop> workshops, @Autowired List<? extends WorkshopInterface> workshopList) {
        this.webClient = webClient;
        this.workshops = workshops;
        this.workshopList = workshopList;
    }

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromIterable(workshops);
    }

    @GetMapping(value = "/{workshop}/tire-change-times")
    public Flux<AvailableChangeTime> getAvailableTimes( @PathVariable List<String> workshop,
                                                        @RequestParam String from,
                                                        @RequestParam String until,
                                                        @RequestParam(required = false, defaultValue = "ALL") String vehicle){

        // Filter workshops by workshop name and vehicle type
        List<? extends WorkshopInterface> workshopsToGetTimesFor = workshopList.stream()
                .filter(w -> workshop.contains("All") || workshop.contains(w.getWorkshop().getName()))
                .filter(w -> vehicle.equals("ALL") || w.getWorkshop().getVehicles().contains(Workshop.VehicleType.valueOf(vehicle)))
                .toList();

        // Return available times for all matching workshops
        return Flux.fromStream(workshopsToGetTimesFor.stream())
                .flatMap(w -> w.getAvailableChangeTime(from, until));

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

}