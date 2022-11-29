package com.karlaru.tcw.workshops;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.BookingResponse;
import com.karlaru.tcw.response.models.ContactInformation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkshopInterface {

    public Workshop getWorkshop();
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until);
    public Mono<BookingResponse> bookChangeTime(Object id, Mono<ContactInformation> contactInformation);
}
