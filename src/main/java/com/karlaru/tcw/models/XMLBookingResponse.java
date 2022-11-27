package com.karlaru.tcw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@AllArgsConstructor
@NoArgsConstructor
@Data
@XmlRootElement(name = "tireChangeBookingResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLBookingResponse {
    @XmlElement(name = "uuid")
    private String uuid;
    @XmlElement(name = "time")
    private String time;
}
