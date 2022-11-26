package com.karlaru.tcw.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableChangeTime {

    private Workshop workshop;
    private boolean available;
    private String uuid;
    private int id;
    private String time;

    // Manchester
    public AvailableChangeTime(boolean available, int id, String time) {
        this(null, available,null, id, time);
    }

    // London
    public AvailableChangeTime(String time, String uuid) {
        this(null, true, uuid, -1, time);
    }
}
