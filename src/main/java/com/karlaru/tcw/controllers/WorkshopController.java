package com.karlaru.tcw.controllers;

import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.ErrorException;
import com.karlaru.tcw.exceptions.NotFoundException;
import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.workshops.Workshop;
import com.karlaru.tcw.workshops.WorkshopInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workshop")
public class WorkshopController {

    private final List<? extends WorkshopInterface> workshopList;

    @Operation(summary = "Get available workshops")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Workshop.class))})})
    @GetMapping
    public Flux<Workshop> getWorkshops(){
        return Flux.fromStream(workshopList.stream())
                .map(WorkshopInterface::getWorkshop)
                .switchIfEmpty(
                        Flux.error(new NotFoundException(HttpStatus.NOT_FOUND.value(), "Workshop list is empty!")))
                .onErrorMap(Predicate.not(NotFoundException.class::isInstance),
                        throwable -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        "Something went with getting Workshop list!"));
    }

    @Operation(summary = "Search for available times")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AvailableChangeTime.class))})})
    @GetMapping(value = "/{workshops}/tire-change-times")
    public Flux<AvailableChangeTime> getAvailableTimes(@PathVariable List<String> workshops,
                                                       @RequestParam List<String> vehicles,
                                                       @RequestParam String from,
                                                       @RequestParam String until){

        // Filter by Name
        List<? extends WorkshopInterface> suitableWorkshops = this.workshopList
                .stream()
                .filter(w -> workshops.contains(w.getWorkshop().name()))
                .toList();

        if (suitableWorkshops.size() == 0)
            return Flux.error(
                    new NotFoundException(HttpStatus.NOT_FOUND.value(), "Workshop " + workshops + " is not found"));


        // Filter by Vehicle
        suitableWorkshops = suitableWorkshops
                .stream()
                .filter(w -> {
                    for (String vehicle: vehicles) {
                        try {
                            if (w.getWorkshop().vehicles().contains(Workshop.VehicleType.valueOf(vehicle))) {
                                return true;
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return false;
                })
                .toList();

        if (suitableWorkshops.size() == 0)
            return Flux.error(
                    new BadRequestException(HttpStatus.BAD_REQUEST.value(), workshops + " workshop doesn't change " + vehicles));

        // Validate date-time
        try {
            ZonedDateTime fromZDT = ZonedDateTime.parse(from + "T00:00:00Z");
            ZonedDateTime untilZDT = ZonedDateTime.parse(until + "T00:00:00Z");
            if (untilZDT.isBefore(fromZDT)) {
                return Flux.error(
                        new BadRequestException(HttpStatus.BAD_REQUEST.value(), "From date is after Until date"));
            }
        }catch (Exception e){
            return Flux.error(new BadRequestException(HttpStatus.BAD_REQUEST.value(), "Invalid date format"));
        }

        // Return aggregated data for 1 or more workshops
        return Flux.fromStream(suitableWorkshops.stream())
                .flatMap(w -> w.getAvailableChangeTime(from, until));
    }


    @Operation(summary = "Book available time")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))})})
    @PutMapping(value = "/{workshop}/tire-change-times/{id}/booking", consumes = "application/json")
    public ResponseEntity<Mono<Booking>> bookAvailableTime(@PathVariable String workshop,
                                                           @PathVariable String id,
                                                           @RequestBody Mono<ContactInformation> contactInformation){

        WorkshopInterface bookWorkshop = getWorkshop(workshop);

        if (bookWorkshop == null){
            return getResponse(
                    HttpStatus.NOT_FOUND,
                    Mono.error(new NotFoundException(HttpStatus.NOT_FOUND.value(), workshop + " not found")));
        }
        return getResponse(
                HttpStatus.OK,
                bookWorkshop.bookChangeTime(id, contactInformation));

    }

    private WorkshopInterface getWorkshop(String workshop) {
        return workshopList.stream()
                .filter(w -> w.getWorkshop().name().equals(workshop))
                .findAny()
                .orElse(null);
    }
    private static ResponseEntity<Mono<Booking>> getResponse(HttpStatus httpStatus, Mono<Booking> bookingMono) {
        return ResponseEntity
                .status(httpStatus)
                .body(bookingMono);
    }
}