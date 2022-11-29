package com.karlaru.tcw.response.models;

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
public class BookingResponse {
    private boolean available;
    private int id;
    @XmlElement(name = "time")
    private String time;
    @XmlElement(name = "uuid")
    private String uuid;


    public BookingResponse(String time, String uuid) {
        this.time = time;
        this.uuid = uuid;
    }

    public BookingResponse(boolean available, int id, String time) {
        this.available = available;
        this.id = id;
        this.time = time;
    }
}
