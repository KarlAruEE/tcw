package com.karlaru.tcw.controllers;

import com.karlaru.tcw.models.AvailableChangeTime;
import com.karlaru.tcw.models.BookingResponse;
import com.karlaru.tcw.models.ContactInformation;
import com.karlaru.tcw.models.Workshop;
import com.karlaru.tcw.workshops.WorkshopInterface;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    private final List<? extends WorkshopInterface> workshopList;

    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromStream(workshopList.stream())
                .map(WorkshopInterface::getWorkshop);
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
    public Mono<BookingResponse> bookAvailableTime( @PathVariable String workshop,
                                                        @PathVariable Object id,
                                                        @RequestBody Mono<ContactInformation> contactInformation){

        WorkshopInterface bookWorkshop = workshopList.stream()
                .filter(w -> w.getWorkshop().getName().equals(workshop))
                .findAny()
                .get();
        return bookWorkshop.bookChangeTime(id, contactInformation);

    }
}