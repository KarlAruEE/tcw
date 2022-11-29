package com.karlaru.tcw.workshops;

import com.karlaru.tcw.models.AvailableChangeTime;
import com.karlaru.tcw.models.BookingResponse;
import com.karlaru.tcw.models.ContactInformation;
import com.karlaru.tcw.models.Workshop;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkshopInterface {

    public Workshop getWorkshop();
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until);
    public Mono<BookingResponse> bookChangeTime(Object id, Mono<ContactInformation> contactInformation);
}
