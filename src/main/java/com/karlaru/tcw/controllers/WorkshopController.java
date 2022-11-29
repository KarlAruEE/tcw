package com.karlaru.tcw.controllers;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.BookingResponse;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.workshops.Workshop;
import com.karlaru.tcw.workshops.WorkshopInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    private final List<? extends WorkshopInterface> workshopList;

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromStream(workshopList.stream())
                .map(WorkshopInterface::getWorkshop)
                .timeout(Duration.ofSeconds(10), Flux.empty());
    }

    @GetMapping(value = "/{workshop}/tire-change-times")
    public ResponseEntity<Flux<AvailableChangeTime>> getAvailableTimes(@PathVariable List<String> workshop,
                                                                      @RequestParam String from,
                                                                      @RequestParam String until,
                                                                      @RequestParam(required = false, defaultValue = "ALL") String vehicle){

        // Get workshops by workshop name and vehicle type
        List<? extends WorkshopInterface> workshopsToGetTimesFor = workshopList.stream()
                .filter(w -> workshop.contains("All") || workshop.contains(w.getWorkshop().name()))
                .filter(w -> vehicle.equals("ALL") || w.getWorkshop().vehicles().contains(Workshop.VehicleType.valueOf(vehicle)))
                .toList();

        if (workshopsToGetTimesFor.size() == 0)
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Flux.empty());

        // Return available times for all matching workshops
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Flux.fromStream(workshopsToGetTimesFor.stream())
                        .flatMap(w -> w.getAvailableChangeTime(from, until))
                        .timeout(Duration.ofSeconds(10), Flux.empty()));
    }

    @PostMapping(value = "/{workshop}/tire-change-times/{id}/booking", consumes = "application/json")
    public ResponseEntity<Mono<BookingResponse>> bookAvailableTime( @PathVariable String workshop,
                                                                    @PathVariable Object id,
                                                                    @RequestBody Mono<ContactInformation> contactInformation){

        WorkshopInterface bookWorkshop = workshopList.stream()
                .filter(w -> w.getWorkshop().name().equals(workshop))
                .findAny()
                .orElse(null);

        if (bookWorkshop == null){
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Mono.empty());
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookWorkshop.bookChangeTime(id, contactInformation)
                        .timeout(Duration.ofSeconds(10), Mono.empty()));

    }
}