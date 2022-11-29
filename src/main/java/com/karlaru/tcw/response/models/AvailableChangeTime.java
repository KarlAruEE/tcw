package com.karlaru.tcw.response.models;


import com.karlaru.tcw.workshops.Workshop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableChangeTime {

    private Workshop workshop;
    private boolean available;
    private String uuid;
    private int id;
    private ZonedDateTime time;

    // Manchester
    public AvailableChangeTime(boolean available, int id, ZonedDateTime time) {
        this(null, available, null, id, time);
    }

    // London
    public AvailableChangeTime(ZonedDateTime time, String uuid) {
        this(null, true, uuid, -1, time);
    }
}
