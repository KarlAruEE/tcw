package com.karlaru.tcw.workshops;

import com.karlaru.tcw.response.models.AvailableChangeTime;
import com.karlaru.tcw.response.models.Booking;
import com.karlaru.tcw.response.models.ContactInformation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkshopInterface {

    public Workshop getWorkshop();
    public Flux<AvailableChangeTime> getAvailableChangeTime(String from, String until);
    public Mono<Booking> bookChangeTime(Object id, Mono<ContactInformation> contactInformation);
    public void setMockUrl(String mockUrl);
}
