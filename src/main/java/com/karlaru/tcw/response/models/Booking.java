package com.karlaru.tcw.response.models;

import com.karlaru.tcw.workshops.Workshop;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement(name = "tireChangeBookingResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class Booking {
    private boolean available;
    @XmlElement(name = "time")
    private String time;
    @XmlElement(name = "uuid")
    private String id;
    private Workshop workshop;


    public Booking(String time, String id) {
        this.available = false;
        this.time = time;
        this.id = id;
    }

    public Booking(boolean available, int id, String time) {
        this.available = available;
        this.id = String.valueOf(id);
        this.time = time;
    }
}
