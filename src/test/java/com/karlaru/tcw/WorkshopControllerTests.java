package com.karlaru.tcw;

import com.karlaru.tcw.controllers.WorkshopController;
import com.karlaru.tcw.exceptions.BadRequestException;
import com.karlaru.tcw.exceptions.NotFoundException;
import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import com.karlaru.tcw.workshops.Workshop;
import com.karlaru.tcw.workshops.WorkshopInterface;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkshopControllerTests {

    private final WorkshopController workshopController = new WorkshopController(List.of(
            new WorkshopInterface() {
                @Override
                public Workshop getWorkshop() {
                    return new Workshop("Test WS 1", "Loc 1", List.of(Workshop.VehicleType.Car));
                }

                @Override
                public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
                    return Flux.fromIterable(List.of(
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"ID-01"),
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"ID-02")));
                }

                @Override
                public Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation) {
                    return Mono.just(new Booking("2022-12-01T10:00:00Z","ID-01"));
                }
            },
            new WorkshopInterface() {
                @Override
                public Workshop getWorkshop() {
                    return new Workshop("Test WS 2", "Loc 2", List.of(Workshop.VehicleType.Truck));
                }

                @Override
                public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
                    return Flux.fromIterable(List.of(
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-02T10:00:00Z"),"I-01"),
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-02T11:10:10Z"),"I-02")));
                }

                @Override
                public Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation) {
                    return Mono.just(new Booking("2022-12-02T10:00:00Z","I-01"));
                }
            },
            new WorkshopInterface() {
                @Override
                public Workshop getWorkshop() {
                    return new Workshop("Test WS 3", "Loc 3", List.of(Workshop.VehicleType.Car,
                                                                                    Workshop.VehicleType.Truck));
                }

                @Override
                public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until) {
                    return Flux.fromIterable(List.of(
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"IX-01"),
                            new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"IX-02")));
                }

                @Override
                public Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation) {
                    return Mono.just(new Booking("2022-12-01T10:00:00Z","IX-01"));
                }
            }

    ));
    private final WorkshopController emptyWorkshopController = new WorkshopController(List.of());


    @Test
    void shouldGetWorkshops(){
        Flux<Workshop> response = workshopController.getWorkshops();

        StepVerifier
                .create(response)
                .expectNextMatches(w -> w.name().equals("Test WS 1") &&
                                        w.address().equals("Loc 1") &&
                                        w.vehicles().contains(Workshop.VehicleType.Car))
                .expectNextMatches(w -> w.name().equals("Test WS 2") &&
                                        w.address().equals("Loc 2") &&
                                        w.vehicles().contains(Workshop.VehicleType.Truck))
                .expectNextMatches(w -> w.name().equals("Test WS 3") &&
                                        w.address().equals("Loc 3") &&
                                        w.vehicles().contains(Workshop.VehicleType.Car) &&
                                        w.vehicles().contains(Workshop.VehicleType.Truck))
                .verifyComplete();
    }

    @Test
    void shouldReturnWorkshopListEmpty(){
        Flux<Workshop> response = emptyWorkshopController.getWorkshops();

        StepVerifier
                .create(response)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                                                 throwable.getMessage().contains("Workshop list is empty!"))
                .verify();
    }

    @Test
    public void shouldReturnNoWorkshops(){
        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(
                        List.of("Wrong Workshop"), List.of("Car"), "2022-11-01", "2022-11-02");

        StepVerifier
                .create(changeTimeFlux)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Workshop [Wrong Workshop] is not found"))
                .verify();

    }

    @Test
    public void shouldReturnWrongVehicle(){
        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(
                        List.of("Test WS 2"), List.of("Tractor", "Van"), "2022-11-01", "2022-11-02");

        StepVerifier
                .create(changeTimeFlux)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                                    throwable.getMessage().equals("[Test WS 2] workshop doesn't change [Tractor, Van]"))
                .verify();

    }

    @Test
    public void shouldReturnWrongDateFormat(){
        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(List.of("Test WS 3"), List.of("Car"), "2022-11-01", "2022-11-2");

        StepVerifier
                .create(changeTimeFlux)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().equals("Invalid date format"))
                .verify();

    }

    @Test
    public void shouldReturnUntilBeforeFrom(){
        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(List.of("Test WS 3"), List.of("Car"), "2022-12-01", "2022-11-13");

        StepVerifier
                .create(changeTimeFlux)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().equals("From date is before Until date"))
                .verify();

    }

    @Test
    public void shouldReturnAvailableTimes(){
        List<AvailableChangeTime> changeTimeCorrect = List.of(
                        new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"ID-01"),
                        new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"ID-02"),
                        new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"IX-01"),
                        new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"IX-02"));

        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(List.of("Test WS 1","Test WS 3"), List.of("Car"), "2022-12-01", "2022-12-02");

        StepVerifier
                .create(changeTimeFlux)
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(0).getTime()) &&
                                        w.getId().equals(changeTimeCorrect.get(0).getId()))
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(1).getTime()) &&
                                        w.getId().equals(changeTimeCorrect.get(1).getId()))
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(2).getTime()) &&
                                        w.getId().equals(changeTimeCorrect.get(2).getId()))
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(3).getTime()) &&
                                        w.getId().equals(changeTimeCorrect.get(3).getId()))
                .verifyComplete();

    }

    @Test
    public void shouldReturnFilteredByTruckAvailableTimes(){
        List<AvailableChangeTime> changeTimeCorrect = List.of(
                new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"ID-01"),
                new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"ID-02"),
                new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T10:00:00Z"),"IX-01"),
                new AvailableChangeTime(ZonedDateTime.parse("2022-12-01T11:10:10Z"),"IX-02"));

        Flux<AvailableChangeTime> changeTimeFlux =
                workshopController.getAvailableTimes(List.of("Test WS 1","Test WS 3"), List.of("Truck"), "2022-12-01", "2022-12-02");

        StepVerifier
                .create(changeTimeFlux)
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(2).getTime()) &&
                        w.getId().equals(changeTimeCorrect.get(2).getId()))
                .expectNextMatches(w -> w.getTime().isEqual(changeTimeCorrect.get(3).getTime()) &&
                        w.getId().equals(changeTimeCorrect.get(3).getId()))
                .verifyComplete();

    }
}
