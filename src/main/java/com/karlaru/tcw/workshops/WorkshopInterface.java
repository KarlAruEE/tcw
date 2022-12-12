package com.karlaru.tcw.workshops;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkshopInterface {

    Workshop getWorkshop();
    Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until);
    Mono<Booking> bookChangeTime(String id, Mono<ContactInformation> contactInformation);
}
