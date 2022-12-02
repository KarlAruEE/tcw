package com.karlaru.tcw.response.models;


import com.karlaru.tcw.workshops.Workshop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Data
@NoArgsConstructor
public class AvailableChangeTime {

    private Workshop workshop;
    private boolean available;
    private String id;
    private ZonedDateTime time;

    public AvailableChangeTime(Workshop workshop, boolean available, String id, ZonedDateTime time) {
        this.workshop = workshop;
        this.available = available;
        this.id = id;
        this.time = time;
    }

    // Manchester
    public AvailableChangeTime(boolean available, int id, ZonedDateTime time) {
        this(null, available, String.valueOf(id), time);
    }

    // London
    public AvailableChangeTime(ZonedDateTime time, String uuid) {
        this(null, true, uuid, time);
    }
}
